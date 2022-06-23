package android.example.paintapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/*VISUALIZAR LA IMAGEN*/

/* Cuando estoy en la vista de grilla donde puedo ver todas las imágenes
* puedo seleccionar cualquiera de ellas
* Y me llevará a una vista, donde podré ver la imagen en tamaño completo
*/
public class ViewFileAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Es solo un image view gigante que abarca toda la vista
        setContentView(R.layout.activity_view_file);

        //Recibe la imagen enviada de FilesAdapters
        Intent intent = getIntent();
        if(intent!=null){
            ImageView imageView = findViewById(R.id.image);
            //Toma la imagen que viene de FilesAdapters y la coloca
            imageView.setImageURI(intent.getData());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
