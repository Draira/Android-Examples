package android.example.paintapp.widget;

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

/*-----------------------------------------------------------------------------------
*       Paint App - Hazem Ourari
*------------------------------------------------------------------------------------
*
* https://www.youtube.com/watch?v=83-j3K4cta8&list=PLW98DQDDUrRhUf167J9K8KmwoK6iUA4Gi&index=1&ab_channel=HazemOurari
*
* Acá no se desaparece el bitmap cuando se aumenta el brush, pero a cambio no se rota ni se resize
* Pasa que cuando se mueve el brush size la imagen del bitmap desaparece... por qué? ni idea.
*La imagen y la ubicación siguen en pie, pero desaparece de la vista del usuario solo al cambiar el tamaño del brush, pero no de la goma o las imagenes
*
* Ya pasa que cuando tomove es false ahí la imagen desaparece
*
* Ya se me ocurre como mejorarlo, en hacer una imagen clickeable, debo hacer un proyecto paint, pero nuevo
* Esta vez usando el tutorial más básico que utilicé alguna vez, yaaas!
* */
public class PaintView extends View {

    private static final String TAG = "PaintView";

    //Creo diferentes bitmaps, en este orden: fondo, vista, imagen, copiaimapen, imagen original e imagen rotada
    private Bitmap btmBackground, btnView, imagee, captureImage, originalImage, rotateImage;
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
    private int leftImage = 0, topImage = 0; //Posición (x,y) iniciales donde aparecerá la imagen, (x,y) no es el centro de la imagen, sino la esquina superior izquierda
    public static boolean toMove = false; //Cuando toMove sea falso se escribirá, pero si es true, podrá mover la imagen

    private float refX, refY;


    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Toast.makeText(context, "Crea un Paint", Toast.LENGTH_SHORT).show();
        
        //FragmentManager.findFragment(this)
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


        if(imagee != null){
            canvas.drawBitmap(imagee, leftImage, topImage, null);//Dibuja la imagen cuya esquina supeior izquierda se encuentra en la posición (leftImage, topImage)
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

                if(imagee !=null){

                    /*
                    *
                    *           .(leftImage, topImage)  ----> x mayor
                    *           **********
                    *           *        *
                    *           *        *
                    *           *        *
                    *           *        *
                    *           **********
                    *           |
                    *           |
                    *           y mayor
                    * */
                   //Basicamente acá estas tocando la imagen
                    if((refX >= leftImage && refX <= imagee.getWidth() + leftImage)
                        && (refY >= topImage && refY <= topImage + imagee.getHeight())) {
                        toMove = true;
                    }else{//Y aca no
                        toMove = false;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if(!toMove){//Si tomove es falso
                    touchMove(x,y);//Entonces escribe
                }
                else{//De lo contrario mueve la imagen
                    float nY = event.getY(); //ny toma todos los puntos tocados y cuando mueves el debo por la pantalla
                    float nX = event.getX(); //nx toma todos los puntos tocados x cuando mueves el debo por la pantalla

                    leftImage += nX -refX;
                    topImage += nY - refY;

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

    //Función que indica si se tocó una esquina de la imagen o no
    private boolean isToResize(float refX, float refY) {
        if((refX >= leftImage && refX <leftImage + imagee.getWidth()
        && ((refY >=topImage && refY <= topImage + 20) ||
                (refY >= topImage + imagee.getHeight() - 20
                &&refY <= topImage + imagee.getHeight())))){
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

    public void setImagee(Bitmap bitmap) {
        toMove = true;
        imagee = Bitmap.createScaledBitmap(bitmap, getWidth()/2, getHeight()/2,true);
        //Esto lo añadi yo addLastAction(getBitmap());
        //addLastAction(getBitmap());
        originalImage = imagee;
        //TODO tendría que hacer una lista de imágenes con dimensiones yo creo~


        invalidate();
    }
}

