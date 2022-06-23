package android.example.firebaseexamples;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/************************************************************************
                                VIDEO 13
 ************************************************************************

Antes cuando le hacíamos UPDATE a la lista y refrescábamos, recargábamos todos los elementos
 incluso aquellos que no habían sido modificados, por lo que ahora veremos una forma de obtener solo
 los modificados:

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

public class MainActivity12 extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDesciption;
    private EditText editTextPriority;
    private TextView textViewData;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private DocumentSnapshot lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main12);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption = findViewById(R.id.edit_text_description);
        editTextPriority = findViewById(R.id.edit_text_priority);
        textViewData = findViewById(R.id.text_view_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        notebookRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    //En una aplicación de verdad se debería colocar un Log aquí
                    //Return es para salir de este método y que no se creshee
                    return;
                }
                for(DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = dc.getDocument();
                    String id = documentSnapshot.getId();
                    int oldIndex = dc.getOldIndex();
                    int newInfex = dc.getNewIndex();

                    switch (dc.getType()){
                        case ADDED:
                            textViewData.append("\nAdded: " + id + "\nOld Index: " + oldIndex + "New Index: " + newInfex);
                            break;
                        case MODIFIED:
                            textViewData.append("\nNodified: " + id + "\nOld Index: " + oldIndex + "New Index: " + newInfex);
                            break;
                        case REMOVED:
                            textViewData.append("\nRemoved: " + id + "\nOld Index: " + oldIndex + "New Index: " + newInfex);
                            break;
                    }
                }
            }
        });
    }

    public void addNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        //Basicamente, si no le pongo prioridad coloca como aleatoria una cero.
        if (editTextPriority.length() == 0) {
            editTextPriority.setText("0");
        }

        int priority = Integer.parseInt(editTextPriority.getText().toString());

        Note8 note = new Note8(title, desciption, priority);
        notebookRef.add(note);
    }

    public void loadNotes(View view) {

        Query query;

        if (lastResult == null) {
            query = notebookRef.orderBy("priority")
                    .limit(3);
        } else {
            query = notebookRef.orderBy("priority")
                    .startAfter(lastResult)
                    .limit(3);
        }

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data = "";

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Note8 note = documentSnapshot.toObject(Note8.class);
                            note.setDocumentId(documentSnapshot.getId());

                            String documentId = note.getDocumentId();
                            String title = note.getTitle();
                            String description = note.getDescription();
                            int priority = note.getPriority();

                            data += "ID: " + documentId
                                    + "\nTitle: " + title
                                    + "\nDesciption" + description
                                    + "\nPriority" + priority + "\n\n";

                        }

                        if (queryDocumentSnapshots.size() > 0) {
                            data += "____________________\n\n";
                            //Si hacemos esto eliminamos la data previa
                            //textViewData.setText(data);
                            textViewData.append(data);

                            lastResult = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size()-1);
                        }
                    }
                });
    }
}