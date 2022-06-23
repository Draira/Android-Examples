package android.example.multiplesbitmaps2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    /*
     * Solución creada por mí
     *
     *  En AddImage se puede añadir una nueva imagen y moverla en el canvas
     *  Mientras que write deja las imágenes estáticas sin poder moverse,
     * pero se puede escribir sobre ellas. Si se agrega una imagen nuevamente se pueden mover
     * cualquiera de ellas en el canvas, pero ya no se puede escribir
     */


    private static final String TAG = "MainActivity";

    PaintView paintView;
    Button addButton;
    Button writeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paintview);
        addButton = findViewById(R.id.button_add);
        writeButton = findViewById(R.id.button_write);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.eren);
                paintView.setImagee(bmp);
            }
        });

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"paintview write is activate: " + paintView.writeIsActive);
                paintView.writeIsActive = true;
                Log.d(TAG,"paintview write is activate: " + paintView.writeIsActive);
            }
        });
    }


}