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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/************************************************************************
                                VIDEO 8
 ************************************************************************

 *DESDE ACÁ TOODO CAMBIA UN POCO*
 *
Hasta el momento, los tutoriales del 2 al 7 han sido solo un mismo documento sobreescrito,
 pero ahora haremos la creación de multiples documentos, creación y guardado

 La aplicación tendrá la siguiente estructura:
 ------------------------
 Title: "...."
 Description: "...."

 Boton Save
 Boton Load

 *Acá se muestra el texto guardado en firestore*
 -----------------------

 La colección del documento se llama: "Notebook"
Cada documento creado tiene su propio ID.

Ahora son multiples documentos creados en la colección "Notebook"
 */

public class MainActivity7 extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDesciption;
    private TextView textViewData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Agregamos acá la referencia a la colección del notebook
    private CollectionReference notebookRef = db.collection("Notebook");

    //Esta es la referencia al documento en particular
    private DocumentReference noteRef = db.document("Notebook/My first note");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption =  findViewById(R.id.edit_text_description);
        textViewData = findViewById(R.id.text_view_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Por lo que usamos le referenca a la colección para escuchar
        notebookRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    return;
                }

                String data = "";

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    //Tomamos la Nota de Cloud FireStore y lo transformamos a un objeto de tipo Note
                    Note7 note = documentSnapshot.toObject(Note7.class);

                    //Lo que hace aquí es tomar el ID del documento específico de Cloud Firestore
                    //Y asigna ese ID a la Nota.java
                    note.setDocumentId(documentSnapshot.getId());

                    String title = note.getTitle();
                    String description = note.getDescription();
                    String documentId = note.getDocumentId();

                    data += "ID: " + documentId + "Title: " + title + "\nDescription: " + description + "\n\n";
                }
                textViewData.setText(data);
            }
        });
    }

    public void addNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        Note7 note = new Note7(title, desciption);
        //Añadiremos la nota a la colección, esto hará que se genere un nuevo ID de documento
        //por cada nota creada
        notebookRef.add(note);
    }

    public void loadNotes(View view) {
        //Luego obtenemos toda la colección de vuelta, es decir todos los documentos
        notebookRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data ="";
                        //QuerySnapshot vendría a ser la lista que contiene tooodos los documentos
                        //Por lo cual si queremos visualizar datos habría que recorrerlo
                        //QueryDocumentSnapshot vendría a ser la reperentación de 1 solo documento
                        // similar a DocumentSnapshot
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Note7 note = documentSnapshot.toObject(Note7.class);
                            note.setDocumentId(documentSnapshot.getId());

                            String title = note.getTitle();
                            String description = note.getDescription();
                            String documentId = note.getDocumentId();

                            data += "ID: " + documentId + "Title: " + title + "\nDescription: " + description + "\n\n";
                        }
                        textViewData.setText(data);
                    }
                });
    }
}