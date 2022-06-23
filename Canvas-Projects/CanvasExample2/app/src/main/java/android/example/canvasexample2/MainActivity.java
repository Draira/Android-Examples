package android.example.canvasexample2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/*
Es el mismo ejemplo que Canvas Example pero con 2 diferencias
    *El custom view se hace desde el XML del activity_main.xml
    *Se ajustó el tamaño del custom view y se recortaron los bordes
*/
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}