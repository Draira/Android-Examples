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
import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.res.Resources;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view that follows touch events to draw on a canvas.
 */

public class MyCanvasView extends View {

    private static final String TAG = "MyCanvasView";

    private Paint mPaint;
    private Path mPath;
    private int mDrawColor;
    private int mBackgroundColor;
    private Canvas mExtraCanvas;
    private Bitmap mExtraBitmap;
    private Rect mFrame;

    private List<Coordinates> pathCoordinates; //Lista de puntos atravezados en la pantalla
    private List<Stroke> rayas = new ArrayList<Stroke>(); //Lista de trazos

    FirebaseFirestore fStore = FirebaseFirestore.getInstance(); //Instancia de firebase
    DocumentReference docref = fStore.collection("Agenda1").document("Hoja1");//Referencia al documento

    MyCanvasView(Context context) {
        this(context, null);
    }

    public MyCanvasView(Context context, AttributeSet attributeSet) {
        super(context);
        mBackgroundColor = ResourcesCompat.getColor(getResources(), R.color.opaque_orange, null);
        mDrawColor = ResourcesCompat.getColor(getResources(), R.color.opaque_yellow, null);

        mPath = new Path(); //Contine el path en el que estamos actualmente dibujando
        mPaint = new Paint();
        mPaint.setColor(mDrawColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        //Se necesita saber si hay un path anteriormente creado o no
        docref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                if(error != null){
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error " + error);
                }

                if(documentSnapshot.exists()) {//Si el documento existe recuperar la hoja
                    Sheet hojaRecuperada = documentSnapshot.toObject(Sheet.class);//Recuperamos la hoja
                    if(hojaRecuperada == null){//Comprobamos si es nula o tiene algún elemento
                        Log.d(TAG, "No existe el documento");
                    }
                    else {
                        Log.d(TAG, "Existe el documento");
                        for(Stroke trazoRecuperado: hojaRecuperada.strokes){//Recuperamos cada trazo
                            rayas.add(trazoRecuperado); //Se añaden los elementos recuperados a la lista de trazos
                            drawSegment(trazoRecuperado, paintFromColor( trazoRecuperado.getColor())); //Y se dibujan los segmentos recuperados
                        }
                    }
                }
            }
        });
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        Log.d(TAG, " onSizeChanged");
        mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mExtraCanvas = new Canvas(mExtraBitmap);
        mExtraCanvas.drawColor(mBackgroundColor);
        int inset = 40;
        mFrame = new Rect (inset, inset, width - inset, height - inset);
    }

@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);
        canvas.drawRect(mFrame, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        pathCoordinates = new ArrayList<Coordinates>(); //Inicializamos la lista de puntos
        Coordinates point = new Coordinates(mX, mY);
        pathCoordinates.add(point);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            mExtraCanvas.drawPath(mPath, mPaint);

            Coordinates point = new Coordinates(mX, mY); //Guarda el path
            pathCoordinates.add(point);
        }
    }

    private void touchUp() {
        Stroke stroke = new Stroke(pathCoordinates, mPaint.getColor(), mPaint.getStrokeWidth()); //Guarda el path en el stroke
        rayas.add(stroke);
        Sheet sheet = new Sheet(mBackgroundColor, rayas);
        docref.set(sheet).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Path añadido a firebase", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Path error a firebase: "+e, Toast.LENGTH_SHORT).show();
            }
        });

        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            default:
        }
        return true;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Toast.makeText(getContext(), "fui destruida", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDetachedFromWindow");
    }


    /*Los siguientes métodos son para recuperar los puntos cuando hubo una rotación*/

    private void drawSegment(Stroke stroke, Paint paint) {
        //Si el canvas mBuffer es distinto de null, dibuja un Path
        //Basicamente si hay canvas dibuja el path con el paint
        Log.d(TAG, "drawSegment");
        if (mExtraCanvas != null) {
            mExtraCanvas.drawPath(getPathForPoints(stroke.getPoints()), paint);
            invalidate();
        }
    }

    //Para redibujar el path
    public Path getPathForPoints(List<Coordinates> points) {
        Log.d(TAG, "getPathForPoints");
        Path path = new Path();
        //Point contiene 2 enteros, una coordenada (x,y)
        Coordinates current = points.get(0);
        path.moveTo(current.x, current.y);
        Coordinates next = null;
        for (int i = 1; i < points.size(); ++i) {

            next = points.get(i);
            path.quadTo(
                   current.x, current.y,
                    (next.x + current.x) / 2, (next.y + current.y) / 2
            );
            current = next;
        }
        if (next != null) {
            path.lineTo(Math.round(next.x), Math.round( next.y));
        }
        return path;
    }

    //Retorna un Paint, básicamente un color y el estilo del trazo
    public static Paint paintFromColor(int color) {
        Log.d(TAG, "Paint paintFromColor 1");
        return paintFromColor(color, Paint.Style.STROKE);
    }

    //Retorna lo mismo que el anterior, pero ahora se le pasa un estilo diferente
    public static Paint paintFromColor(int color, Paint.Style style) {
        Log.d(TAG, "Paint paintFromColor 2");
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(12);
        return p;
    }
}
