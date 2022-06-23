/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.canvasexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.content.res.Resources;

/*
 * Custom view que permite dibujar en el canvas recuperado de los codelabs de Google.
 */

public class MyCanvasView extends View {

    private Paint mPaint; //Vairable para establecer los campos del dibujo
    private Path mPath; //Camino recorrido por el dibujo
    private int mDrawColor; //Variable para el color del dibujo
    private int mBackgroundColor; //Variable para el color del fondo
    private Canvas mExtraCanvas; //Lienzo sobre el cual vamos a dibujar
    private Bitmap mExtraBitmap; //El lienzo o canvas respaldará lo dibujado en el bitmap
    private Rect mFrame; //Variable para almacenar un cuadro alrededor del canvas


    MyCanvasView(Context context) {
        this(context, null);
    }

    public MyCanvasView(Context context, AttributeSet attributeSet) {
        super(context);

        mBackgroundColor = ResourcesCompat.getColor(getResources(), R.color.opaque_orange, null);
        mDrawColor = ResourcesCompat.getColor(getResources(), R.color.opaque_yellow, null);

        mPath = new Path();//Contiene el path del dibujo actua
        mPaint = new Paint(); //El paint con el cual se dibuja
        mPaint.setColor(mDrawColor); //Se agrega el color al paint
        mPaint.setAntiAlias(true); //Suaviza los bordes del dibujo
        mPaint.setDither(true); //Afecta cómo se ven los colores
        mPaint.setStyle(Paint.Style.STROKE); // default: FILL, con stroke se generará un trazo al dibujar
        mPaint.setStrokeJoin(Paint.Join.ROUND); // default: MITER, hace que la unión de los trazos sea más redondeado
        mPaint.setStrokeCap(Paint.Cap.ROUND); // default: BUTT, hace que los bordes inicial y final sean más redondeados
        mPaint.setStrokeWidth(12); //Grosor del dibujo
    }

    /**
     *onSizeChanged se llama cada vez que la vista cambia de tamaño.
     *Dado que la vista comienza sin tamaño, esto también se llama después
     *del inflate.
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //Crea un bitmap
        mExtraCanvas = new Canvas(mExtraBitmap);//Se le añade el bitmap creado al canvas
        mExtraCanvas.drawColor(mBackgroundColor);//Se agrega el color de fondo

        // Calcula el marco alrededor del canvas.
        int inset = 40;
        mFrame = new Rect (inset, inset, width - inset, height - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dibuja el bitmap guardado en el path.
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);

        // Dibuja el marco alrededor del lienzo.
        canvas.drawRect(mFrame, mPaint);
    }

    // Variables para los últimos valores x,y,
    //que son el punto de partida para el siguiente camino.
    private float mX, mY;
    //La tolerancia es la distancia mínima para poder dibujar,
    //si la distancia que efectúa el dedo es menor a esto no se genera un dibujo
    private static final float TOUCH_TOLERANCE = 4;

    // onTouchEvent() es la gestura de los dedos al tocar la pantalla
    private void touchStart(float x, float y) { //touchStart es cuando los dedos tocan un punto en la pantalla
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {//luego de tocar un punto en la pantalla con touchStart, al moverlos se usa el método touchMove
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            //Genera la unión entre los puntos para generar la figura
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            mExtraCanvas.drawPath(mPath, mPaint); //Dibuja el path en el bitmap extra y lo guarda ahí
        }
    }

    private void touchUp() {//Es el punto en que los dedos dejan la pantalla
        mPath.reset(); //Resetea el path
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Invalidate() es para hacer efectivos los cambios
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                // Acá no se necesita invalidate porque no estamos dibujando nada
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            default:
                //no hace nada
        }
        return true;
    }
    //Obtiene el ancho de la pantalla
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    //Obtiene la altura de la pantalla
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
