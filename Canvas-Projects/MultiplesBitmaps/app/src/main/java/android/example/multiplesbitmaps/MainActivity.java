package android.example.multiplesbitmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    /*-----------------------------------------------------------------------------------------------------
     *   Solución recuperada de StackOverflow @author rajeshcp
     * https://stackoverflow.com/questions/20881790/how-to-move-multiple-bitmaps-in-single-canvas-android
     *
     *  Hay 5 rectángulos que puedo mover dentro de un canvas y cambiar su posición
     *  Los comentarios en este documento son proporcionados por mí.
     *   Sin embargo, la solución que plantea podría simplificarse
     * para ello mi solución se encuentra en MultiplesBitmaps2
     *-----------------------------------------------------------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}