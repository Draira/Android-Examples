package android.example.paintapp.widget;

import android.content.Context;
import android.example.paintapp.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class PaintView extends View {

    private static final String TAG = "PaintView";

    //Creo diferentes bitmaps, en este orden: fondo, vista, imagen, copiaimapen, imagen original e imagen rotada
    private Bitmap btmBackground, btnView, image, captureImage, originalImage, rotateImage;
    private Paint mPaint = new Paint();
    private Path mPath;
    private int colorBackground;
    private int sizeBrush, sizeEraser;
    private float mX, mY;
    //Este es el lienzo: canvas
    private Canvas mCanvas;
    private final int DEFERENCE_SPACE = 4;
    private static final float TOUCH_TOLERANCE = 4;

    //Es un arrayList de bitmap, más específicamente sobre lo que escribo:
    private ArrayList<Bitmap> listAction = new ArrayList<>();
    private int leftImage = 50, topImage = 50;
    public static boolean toMove = false;
    private boolean toResize = false;
    private float refX, refY;
    private int xCenter, yCenter;
    private float xRotate, yRotate;
    private int angle = 0;

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

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

        rotateImage = BitmapFactory.decodeResource(getResources(), R.drawable.camera_icon);
        //El capture que aparece acá debe ser si o si un png, ya que lo había
        //Hecho con un xml y no funciona,
        //TODO tengo que colocar una imagen más pequeña
        captureImage = BitmapFactory.decodeResource(getResources(), R.drawable.camera_icon);

        Log.d(TAG, "onCreate image, debería ser null: "+ image);
    }

    private float toPx(int sizeBrush) {
        return sizeBrush*(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //Creo un bitmap para el background (el color del fondo)
        btmBackground = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        //Creo un bitmap solo para lo que escribiré
        btnView = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //Creo un nuevo canvas y lo inicializo con el bitmap sobre el cual escribiré
        mCanvas = new Canvas(btnView);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Cambio el color del fondo
        canvas.drawColor(colorBackground);
        //Y lo agrego al bitmap del fondo
        canvas.drawBitmap(btmBackground,0 , 0 , null);

        //TODO yo quite eso, para ver si no hay problema 16-04
        if(image != null/* && toMove*/){
            drawImage(canvas);
            xCenter = leftImage + image.getWidth()/2 - captureImage.getWidth()/2;
            yCenter = topImage + image.getHeight()/2 - captureImage.getHeight()/2;

            xRotate = leftImage + image.getWidth() + toPx(10);
            yRotate = topImage - toPx(10);

            canvas.drawBitmap(rotateImage, xRotate, yRotate, null);
            canvas.drawBitmap(captureImage, xCenter, yCenter, null);
        }

        //Hago el dibujo path sobre el lienzo
        canvas.drawBitmap(btnView, 0, 0, null );

    }

    private void drawImage(Canvas canvas) {
        Matrix matrix = new Matrix();
        //Hace la rotación de la imagen
        //Set the matrix to rotate by the specified number of degrees, with a pivot point at (px, py).
        matrix.setRotate(angle, image.getWidth()/2, image.getHeight()/2);
        matrix.postTranslate(leftImage, topImage);
        //drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint)
        //Draw the bitmap using the specified matrix.
        canvas.drawBitmap(image, matrix, null);
    }

    public void setColorBackground(int color){
        colorBackground = color;
        invalidate();
    }

    public void setSizeBrush(int s){
        Log.d(TAG, "en el size brush antes"+ image);
        sizeBrush = s;
        mPaint.setStrokeWidth(sizeBrush);
        Log.d(TAG, "en el size brush después"+ image);
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

                if(toMove){

                    if(isToResize(refX, refY)) {
                        toResize = true;
                    }else{
                        toResize = false;
                    }
                    if((refX >= xCenter && refX < xCenter + captureImage.getWidth())
                    && (refY >= yCenter && refY < yCenter + captureImage.getHeight())){

                        Canvas newCanvas = new Canvas(btmBackground);
                        drawImage(newCanvas);
                        invalidate();
                    }

                    if((refX >= xRotate && refX <= xRotate + rotateImage.getWidth())
                        && (refY >= yRotate && refY <= yRotate + rotateImage.getHeight())){
                        angle+=45;
                        invalidate();
                    }

                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(!toMove){
                    touchMove(x,y);
                }
                else{
                    float nY = event.getY();
                    float nX = event.getX();

                    if(toResize){
                        int xScale = 0;
                        int yScale = 0;

                        if(nX > refX){
                            xScale = (int) (image.getWidth() + (nX-refX));
                        }else {
                            xScale = (int) (image.getWidth() - (refX - nX));
                        }
                        if(nY > refY){
                            yScale = (int) (image.getHeight() + (nY-refY));
                        }else {
                            yScale = (int) (image.getHeight() - (refY - nY));
                        }
                        if (xScale > 0 && yScale > 0){
                            image = Bitmap.createScaledBitmap(originalImage, xScale, yScale, false);
                        }

                    }else {
                        leftImage += nX -refX;
                        topImage += nY - refY;
                    }
                    //Solo me faltaba esto para mover bien la imagen
                    refX = nX;
                    refY = nY;

                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if(!toMove){
                    touchUp();
                    //Cuando se suelta el dedo se añade el nuevo bitmap
                    //Todo lo extraño que no entiendo es que en getbitmap solo guarda el lapiz por lo tanto se debe averiguar y arreglar
                    //Pero en el main al hacer get bitmap toma to.do e imagenes
                    addLastAction(getBitmap());
                }
                break;
            default:
                //do nothing
        }

        return true;
    }

    //Función que indica si se tocó una esquina de la imagen o no
    private boolean isToResize(float refX, float refY) {
        if((refX >= leftImage && refX <leftImage + image.getWidth()
        && ((refY >=topImage && refY <= topImage + 20) ||
                (refY >= topImage + image.getHeight() - 20
                &&refY <= topImage + image.getHeight())))){
            return  true;
        }
        return  false;
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

    public void setImage(Bitmap bitmap) {
        toMove = true;
        image = Bitmap.createScaledBitmap(bitmap, getWidth()/2, getHeight()/2,true);
        //Esto lo añadi yo addLastAction(getBitmap());
        //addLastAction(getBitmap());
        originalImage = image;
        Log.d(TAG, "Sube la imagen del storage"+ image);
        invalidate();
    }
}

