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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/************************************************************************
                                VIDEO 10
 ************************************************************************

 En base al proyecto anterior, inspeccionamos las Querys compuestos.
 Es decir por ejemplo, filtrar query por "priority" y por "title", es decir, por 2 campos distintos.

 La aplicación tendrá la siguiente estructura:
 ------------------------
 Title: "...."
 Description: "...."
 Priority "...."

 Boton Save
 Boton Load

 *Acá se muestra el texto guardado en firestore*
 -----------------------

 La colección del documento se llama: "Notebook"
 Cada documento creado tiene su propio ID.

 Ahora son multiples documentos creados en la colección "Notebook"
 */

public class MainActivity9 extends AppCompatActivity {

    private static  final String  TAG = "MainActivity9";

    private static  final String  KEY_TITLE = "title";
    private static  final String  KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextDesciption;
    private EditText editTextPriority;
    private TextView textViewData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private DocumentReference noteRef = db.document("Notebook/My first note");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main9);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption =  findViewById(R.id.edit_text_description);
        editTextPriority = findViewById(R.id.edit_text_priority);
        textViewData = findViewById(R.id.text_view_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //También podemos hacer lo mismo acá, esto es para ordenar las querys
        notebookRef./*.whereEqualTo("priority", 5)*/addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                String data = "";

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Note8 note = documentSnapshot.toObject(Note8.class);

                    note.setDocumentId(documentSnapshot.getId());

                    String title = note.getTitle();
                    String description = note.getDescription();
                    String documentId = note.getDocumentId();
                    int priority = note.getPriority();

                    data += "ID: " + documentId +
                            "\nTitle: " + title +
                            "\nDescription: " + description +
                            "\nPriority: " + priority +"\n\n";
                }
                textViewData.setText(data);
            }
        });
    }

    public void addNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        //Basicamente, si no le pongo prioridad coloca como aleatoria una cero.
        if(editTextPriority.length() == 0){
            editTextPriority.setText("0");
        }

        int priority = Integer.parseInt(editTextPriority.getText().toString());

        Note8 note = new Note8(title, desciption, priority);
        notebookRef.add(note);
    }

    public void loadNotes(View view) {
        notebookRef
                .whereGreaterThanOrEqualTo("priority", 2)
                //.whereEqualTo("priority", 5)
                //Efectivamente la siguiente línea marcada da error, esto es porque no podemos usar filtros con 2 campos diferentes
                //PEro sí en 2 o más filtros en campos iguales
                //.whereLessThanOrEqualTo("title", "Aa")
                // -------------------------------------------------------------------------------------
                //PEro si podemos añadir un equal con un less con campos diferentes:
                .whereEqualTo("title", "Aa")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data ="";

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Note8 note = documentSnapshot.toObject(Note8.class);
                            note.setDocumentId(documentSnapshot.getId());

                            String title = note.getTitle();
                            String description = note.getDescription();
                            String documentId = note.getDocumentId();
                            int priority = note.getPriority();

                            data += "ID: " + documentId +
                                    "\nTitle: " + title +
                                    "\nDescription: " + description +
                                    "\nPriority: " + priority +"\n\n";
                        }
                        textViewData.setText(data);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
        });
    }
}