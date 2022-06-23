package android.example.multiplesbitmaps2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PaintView extends View {
    private static final String TAG = "PaintView";

    //Creo diferentes bitmaps, en este orden: fondo, vista, imagen, copiaimapen, imagen original e imagen rotada
    private Bitmap btmBackground, btnView, imagee;
    private Paint mPaint = new Paint();
    private Path mPath;
    private int colorBackground;
    private int sizeBrush, sizeEraser;
    private float mX, mY;
    //Este es el lienzo: canvas
    private Canvas mCanvas;
    private final int DEFERENCE_SPACE = 4;
    private static final float TOUCH_TOLERANCE = 4;

    Boolean writeIsActive = false;



    //Es un arrayList de bitmap, más específicamente sobre lo que escribo:
    private ArrayList<Bitmap> listAction = new ArrayList<>();
    private int leftImage = 50, topImage = 50; //Posición (x,y) iniciales donde aparecerá la imagen, (x,y) no es el centro de la imagen, sino la esquina superior izquierda
    public static boolean toMove = false; //Cuando toMove sea falso se escribirá, pero si es true, podrá mover la imagen

    private float refX, refY;

    ArrayList<Bitmap> imageList = new ArrayList<>();
    ArrayList<Integer> cornerLeft = new ArrayList<Integer>();
    ArrayList<Integer> cornerTop = new ArrayList<Integer>();
    int pointXY = -1;
    private final int INVALID_INDEX = -1;

    //CONSTRUCTOR
    public PaintView(Context context) {
        super(context);
    }

    //CONSTRUCTOR2
    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Toast.makeText(context, "Crea un Paint", Toast.LENGTH_SHORT).show();

        init();
    }

    private void init() {

        mPath = new Path();
        //Tamaño de la goma y de la brocha
        sizeEraser = sizeBrush = 12;
        colorBackground = Color.WHITE;

        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(toPx(sizeBrush));

        Log.d(TAG, "onCreate image, debería ser null: "+ imagee);
    }

    private float toPx(int sizeBrush) {
        return sizeBrush*(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        btmBackground = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888); //Creo un bitmap para el background (el color del fondo)
        btnView = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); //Creo un bitmap solo para lo que escribiré
        mCanvas = new Canvas(btnView); //Creo un nuevo canvas y lo inicializo con el bitmap sobre el cual escribiré
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(colorBackground); //Cambio el color del fondo
        canvas.drawBitmap(btmBackground,0 , 0 , null); //Y lo agrego al bitmap del fondo

        Log.d(TAG, "imageList in onDraw: " + imageList);


        if(!imageList.isEmpty()){//Si no está vacía entonces muestralo en el canvas baby!
            for(int i = imageList.size()-1; i >= 0; i--){
                canvas.drawBitmap(imageList.get(i), cornerLeft.get(i), cornerTop.get(i), null);
            }
        }

        canvas.drawBitmap(btnView, 0, 0, null ); //Hago el dibujo path sobre el lienzo

    }

    public void setColorBackground(int color){
        Toast.makeText(getContext(), "setColorBack", Toast.LENGTH_SHORT).show();
        colorBackground = color;
        invalidate();
    }

    public void setSizeBrush(int s){
        Log.d(TAG, "en el size brush antes"+ imagee);
        sizeBrush = s;
        mPaint.setStrokeWidth(sizeBrush);
        Log.d(TAG, "en el size brush después"+ imagee);
    }

    public void setBrushColor(int color){
        mPaint.setColor(color);
    }

    public void setSizeEraser(int s){
        sizeEraser = s;
        mPaint.setStrokeWidth(toPx(sizeEraser));
    }

    //Esta es la funcion que se usa para borrar
    public void enableEraser(){
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void desableEraser(){
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.setMaskFilter(null);
    }

    //Añade el nuevo bitmap a la lista
    public void addLastAction(Bitmap bitmap){
        listAction.add(bitmap);
    }

    //Retorna la última acción del lapiz
    public void returnLastAction(){
        //Si hay algún elemento en la lista listAction que contiene los bitmap (bitmap del lapiz)
        if(listAction.size()>0){
            //Remueve el último elemento
            listAction.remove(listAction.size()-1);

            //Si al remover el elemento anteior, la lista sigue siendo mayor a cero
            if(listAction.size()>0){
                //Entonces el bitmap btnView será igual al bitmap anterior
                btnView = listAction.get(listAction.size()-1);
            }
            //Si ya no quedan elementos
            else{
                //Entonces el bitmap btnView será un bitmap nuevo
                btnView = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }
            //Y dibujamos este bitmap en el canvas
            mCanvas = new Canvas(btnView);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y); //El dedo toca la posición x,y de la pantalla
                refY = y; //RefY toma la posición y del dedo
                refX = x; //RefX toma la posición x del dedo

                pointXY = isTouchingImage(refX, refY);

                if(pointXY == -1){
                    //No está tocando ninguna imagen
                    toMove = false;
                }else{
                    toMove = true;
                    leftImage = cornerLeft.get(pointXY);
                    topImage = cornerTop.get(pointXY);
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if(writeIsActive){
                    touchMove(x,y);//Entonces escribe
                }
                else{//De lo contrario mueve la imagen
                    if(toMove){
                        float nY = event.getY(); //ny toma todos los puntos tocados y cuando mueves el debo por la pantalla
                        float nX = event.getX(); //nx toma todos los puntos tocados x cuando mueves el debo por la pantalla

                        leftImage += nX -refX;
                        topImage += nY - refY;

                        cornerLeft.set(pointXY, leftImage);
                        cornerTop.set(pointXY, topImage);

                        //Solo me faltaba esto para mover bien la imagen
                        refX = nX;
                        refY = nY;

                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if(!toMove){
                    touchUp();
                    //Cuando se suelta el dedo se añade el nuevo bitmap
                    //Todo lo extraño que no entiendo es que en getbitmap solo guarda el lapiz
                    //Pero en el main al hacer get bitmap toma todo e imagenes
                    addLastAction(getBitmap());
                }
                break;
            default:
                //do nothing
        }

        return true;
    }

    private void touchUp() {
        mPath.reset();
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(x,y, (x+mX)/2, (y+mY)/2);

            mY = y;
            mX = x;
            //Dibujo el path en el canvas
            mCanvas.drawPath(mPath, mPaint);
            invalidate();
        }
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
    }

    //Esta funcion crea un nuevo bitmap con solo con el nuevo dibujo hecho al parecer
    public Bitmap getBitmap(){
        //Cuando la caché de dibujo está habilitada setDrawingCacheEnabled(true),
        this.setDrawingCacheEnabled(true);
        //la siguiente llamada a getDrawingCache()o buildDrawingCache()dibujará la vista en un mapa de bits
        this.buildDrawingCache();
        //Genera el bitmap de la vista en particular
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        //Se desactiva el cache
        this.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void setImagee(Bitmap bitmap) {
        writeIsActive = false;

        imageList.add(0, bitmap);
        cornerLeft.add(0, 50);
        cornerTop.add(0, 50);

        invalidate();
    }


    //Retorna el índice de la primera imagen si la encuentra, sino retorna -1
    //Si retorna -1 escribe
    //Si retorna algo diferente a -1 mueve la imagen:
    private int isTouchingImage(float x, float y){
        int index = INVALID_INDEX;
        for(int i = 0; i < imageList.size(); i++){
            if((x >= cornerLeft.get(i) && x <= imageList.get(i).getWidth() + cornerLeft.get(i))
                    && (y >= cornerTop.get(i) && y <= cornerTop.get(i) + imageList.get(i).getHeight())){ //rect.contains(x,y) significa que si (x,y) pertenece dentro de las dimensiones del ractángulo retorna true, sino false
                index = i; //index toma la posición donde se encuentra ese rectángulo
                break;
            }
        }
        return index; //retorna la posición del rectángulo
    }


}
