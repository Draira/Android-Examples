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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import java.util.List;
import java.util.Map;

/************************************************************************
                                VIDEO 11
 ************************************************************************

 En base al proyecto anterior, crearemos la query "OR"
Las querys no traen por defecto el filtro "or", por lo que deben implementarse a modo de Task.

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

public class MainActivity10 extends AppCompatActivity {

    private static final String TAG = "MainActivity9";

    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";

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
        setContentView(R.layout.activity_main10);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption = findViewById(R.id.edit_text_description);
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
                if (error != null) {
                    return;
                }

                String data = "";

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Note8 note = documentSnapshot.toObject(Note8.class);

                    note.setDocumentId(documentSnapshot.getId());

                    String title = note.getTitle();
                    String description = note.getDescription();
                    String documentId = note.getDocumentId();
                    int priority = note.getPriority();

                    data += "ID: " + documentId +
                            "\nTitle: " + title +
                            "\nDescription: " + description +
                            "\nPriority: " + priority + "\n\n";
                }
                textViewData.setText(data);
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
        Task task1 = notebookRef.whereLessThan("priority", 2)
                .orderBy("priority")
                .get();

        Task task2 = notebookRef.whereGreaterThan("priority", 2)
                .orderBy("priority")
                .get();

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task1, task2);
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                String data = "";

                for (QuerySnapshot queryDocumentSnapshots : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Note8 note = documentSnapshot.toObject(Note8.class);

                        note.setDocumentId(documentSnapshot.getId());

                        String title = note.getTitle();
                        String description = note.getDescription();
                        String documentId = note.getDocumentId();
                        int priority = note.getPriority();

                        data += "ID: " + documentId +
                                "\nTitle: " + title +
                                "\nDescription: " + description +
                                "\nPriority: " + priority + "\n\n";
                    }
                }
                textViewData.setText(data);
            }
        });
    }
}