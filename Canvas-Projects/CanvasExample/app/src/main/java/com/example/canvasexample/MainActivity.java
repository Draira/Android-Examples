/*
 * Archivo recuperado de los codelabs de Google con algunas modificaciones efectuadas por mí
 */

package com.example.canvasexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyCanvasView myCanvasView;
        myCanvasView = new MyCanvasView(this); //No se necesita un archivo XML, sino la vista creada programáticamente
        myCanvasView.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(myCanvasView);
    }
}
