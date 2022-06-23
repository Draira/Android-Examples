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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/************************************************************************
                                VIDEO 16
 ************************************************************************

Veremos como almacenar Arrays es FireStore, para ello a las notas
 le añadiremos "tags" las cuales serán introducidas de la siguiente manera:
 "tag1,tag2,tag3,tag4,..."

 La aplicación tendrá la siguiente estructura:
 ------------------------
 Title: "...."
 Description: "...."
 Priority "...."
 Task "...."

 Boton Save
 Boton Load

 *Acá se muestra el texto guardado en firestore*
 -----------------------

 La colección del documento se llama: "Notebook"
 Cada documento creado tiene su propio ID.

 Ahora son multiples documentos creados en la colección "Notebook"
 */

public class MainActivity15 extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextDesciption;
    private EditText editTextPriority;
    private TextView textViewData;
    private EditText editTextTags;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main15);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption = findViewById(R.id.edit_text_description);
        editTextPriority = findViewById(R.id.edit_text_priority);
        textViewData = findViewById(R.id.text_view_data);
        editTextTags = findViewById(R.id.edit_text_tags);

        executeTransaction();
    }

    //Si 2 usuarios al mismo tiempo cambiaran a la vez la prioridad NO ocurría un problema
    private void executeTransaction() {

    }


    public void addNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        //Basicamente, si no le pongo prioridad coloca como aleatoria una cero.
        if (editTextPriority.length() == 0) {
            editTextPriority.setText("0");
        }

        int priority = Integer.parseInt(editTextPriority.getText().toString());

        String tagInput = editTextTags.getText().toString();
        //Es muy importante que lo que ponga en la entreda tag sea así: "tag1,tag2" sin espacios y separados por coma
        String[] tagArray = tagInput.split("\\s*,\\s*");
        List<String> tags = Arrays.asList(tagArray);

        Note15 note = new Note15(title, desciption, priority, tags);
        notebookRef.add(note);
    }

    public void loadNotes(View view) {
        notebookRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data = "";

                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Note15 note =documentSnapshot.toObject(Note15.class);
                            note.setDocumentId(documentSnapshot.getId());

                            String documentID = note.getDocumentId();

                            data += "ID: " + documentID;

                            for(String tag : note.getTags()){
                                data +=  "\n-"+ tag;
                            }

                            data += "\n\n";
                        }
                        textViewData.setText(data);
                    }
                });
    }
}