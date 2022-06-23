package android.example.firebaseexamples;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

/***********************************************************************
                                VIDEO 7
 ************************************************************************

 Al tutorial anterior en vez de añadir los elemenentos en forma de Hashmap a Cloud Firestore
 lo haremos a través de una clase Note.java, que llamaremos Note6.java para hacer referencia a
 MainActivity6.java

 La aplicación tendrá la siguiente estructura:
 ------------------------
 Title: "...."
 Description: "...."

 Boton Save
 Boton Load
 Boton Update Description
 Boton Delete Description
 Boton Delete Note

 *Acá se muestra el texto guardado en firestore*
 -----------------------

 La colección del documento se llama: "Notebook"
 Y el documento en sí tiene el ID: "My first note"

 Hasta el momento siempre sobreescribiremos el mismo documento llamado "My first note"
 */
public class MainActivity6 extends AppCompatActivity {

    private static  final String  TAG = "MainActivity3";

    private static  final String  KEY_TITLE = "title";
    private static  final String  KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextDesciption;
    private TextView textViewData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = db.document("Notebook/My first note");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption =  findViewById(R.id.edit_text_description);
        textViewData = findViewById(R.id.text_view_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if(error !=null){
                    Toast.makeText(MainActivity6.this, "Error while loading", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, error.toString());
                }

                if(documentSnapshot.exists()){
                    Note6 note = documentSnapshot.toObject(Note6.class);

                    String title = note.getTitle();
                    String description = note.getDescription();

                    textViewData.setText("Title: " + title +"\n" + "Description: " + description + "\n");
                }
                else{
                    textViewData.setText("");
                }
            }
        });
    }

    public void saveNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        //Creamos ua nueva nota donde agregámos un título y una descripción.
        Note6 note = new Note6(title, desciption);

        //Agregamos esa nota al documento en Cloud FireStore
        noteRef.set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity6.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity6.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public void loadNote(View view) {
        //Tomamos el documento de Cloud FireStore
        noteRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            //Tomamos el documento y tenemos que convertirlo a object pues está tooodo combinado
                            //por así decirlo, y lo convertimos a la estructura de Note.java
                            Note6 note = documentSnapshot.toObject(Note6.class);

                            String title = note.getTitle();
                            String description = note.getDescription();

                            textViewData.setText("Title: " + title +"\n" + "Description: " + description + "\n");
                        } else {
                            Toast.makeText(MainActivity6.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity6.this, "Error!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });
    }

    public void updateDescription(View view) {
        String description = editTextDesciption.getText().toString();

        noteRef.update(KEY_DESCRIPTION, description);
    }

    public void deleteDescription(View view) {
        noteRef.update(KEY_DESCRIPTION, FieldValue.delete());
    }

    public void deleteNote(View view) {
        noteRef.delete();
    }
}