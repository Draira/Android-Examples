package android.example.firebaseexamples;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

/************************************************************************
                            VIDEO 15
 ************************************************************************

Transacciones, veremos como hacer un request UPDATE o DELETE al mismo
 tiempo, sin que una acción se vea afectada por la otra

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
public class MainActivity14 extends AppCompatActivity {

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
        setContentView(R.layout.activity_main14);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption = findViewById(R.id.edit_text_description);
        editTextPriority = findViewById(R.id.edit_text_priority);
        textViewData = findViewById(R.id.text_view_data);

        executeTransaction();
    }

    //Si 2 usuarios al mismo tiempo cambiaran a la vez la prioridad NO ocurría un problema
    private void executeTransaction() {
        //Si no queremos que entregue nada dejamos el método vacío <Void>
        //Pero ahora vamos a querer que entrgue el Long
        db.runTransaction(new Transaction.Function<Long>() {

            @Override
            public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                //Ahora podemos poner multiples read y write transactions
                //No podemos poner ningún read después de un write,
                // por lo tanto tenemos que definir todas nuestras operaciones read y luego todas las operaciones write

                //Para ello necesitamos una referencia al documento que queremos leer
                //Podemos hacer esta nota directo desde la consola o usar una ya creada de antes, pero hay que tener una nota antes
                DocumentReference exampleNoteRef = notebookRef.document("New note");
                DocumentSnapshot exampleNoteSnapshot = transaction.get(exampleNoteRef);

                //Ahora supongamos que queremos recuperar la prioridad de esta "exampleNoteRef"y modificarla
                //En firestore no están guardados los valores como "int" sino como "long"
                long newPriority = exampleNoteSnapshot.getLong("priority") +1;
                transaction.update(exampleNoteRef, "priority", newPriority);
                return newPriority;
            }
        }).addOnSuccessListener(new OnSuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                Toast.makeText(MainActivity14.this, "New priority: " + result, Toast.LENGTH_SHORT).show();
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