package android.example.firebaseexamples;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/***********************************************************************
                                VIDEO 2
************************************************************************

  Este tutorial consiste básicamente en guardar un documento en Cloud FireStore
 con la siguiente estructura:
 ------------------------
 Title: "...."
 Description: "...."

 Boton Save
 -----------------------

 La colección del documento se llama: "Notebook"
 Y el documento en sí tiene el ID: "My first note"
 */

public class MainActivity extends AppCompatActivity {

    private static  final String  TAG = "MainActivity";

    private static  final String  KEY_TITLE = "title";
    private static  final String  KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextDesciption;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDesciption =  findViewById(R.id.edit_text_description);
    }

    public void saveNote(View view) {
        String title = editTextTitle.getText().toString();
        String desciption = editTextDesciption.getText().toString();

        Map<String,Object> note = new HashMap<>();
        note.put(KEY_TITLE, title);
        note.put(KEY_DESCRIPTION, desciption);

        db.collection("Notebook").document("My first note").set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Note Saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }


}