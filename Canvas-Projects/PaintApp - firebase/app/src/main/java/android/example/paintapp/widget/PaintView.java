package android.example.paintapp.widget;

import android.content.Context;
import android.example.paintapp.R;
import android.example.paintapp.model.Coordinates;
import android.example.paintapp.model.Image;
import android.example.paintapp.model.Sheet;
import android.example.paintapp.model.Stroke;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaintView extends View {

    private static final String TAG = "PaintView";


    //Creo diferentes bitmaps, en este orden: fondo, vista, imagen, copiaimapen, imagen original e imagen rotada
    private Bitmap btmBackground, btnView, image, captureImage, originalImage, rotateImage, bitmapRecuperado;
    private Paint mPaint = new Paint();
    private Path mPath;
    private int colorBackground;
    private int sizeBrush, sizeEraser;
    private Boolean isEraser = false;
    private float mX, mY;
    //Este es el lienzo: canvas
    private Canvas mCanvas;
    private final int DEFERENCE_SPACE = 4;
    private static final float TOUCH_TOLERANCE = 1;

    //Es un arrayList de bitmap, más específicamente sobre lo que escribo:
    private ArrayList<Bitmap> listAction = new ArrayList<>();
    //Son las dimensiones de la imagen que voy a subir
    private int leftImage = 50, topImage = 50;
    //toMove = true es para saber que estamos tocando específicamente una parte de la imagen subida para hacer una acción con ella
    //toMove = false quiere decir que no tocamos la imagen, pero si tocamos la pantalla es que haremos un path ya sea trazo o goma
    public static boolean toMove = false;
    //toResize = true es para saber si modificamos las dimensiones de la imagen
    private boolean toResize = false;
    //Creo que se refiere a la posición de la imagen en el canvas que subio el usuario
    private float refX, refY;
    //Posición central de la imagen que sube el usuario
    private int xCenter, yCenter;
    //Son las dimensiones de la imagen pequeña capture que va dentro de la imagen que pone el usuario
    private float xRotate, yRotate;
    //angle es la variable que dará los valores para rotar la imagen
    private int angle = 0;




    private List<Coordinates> pathCoordinates; //Lista de puntos
    private List<Stroke> lines = new ArrayList<Stroke>(); //Lista de rayas (strokes)
    private List<Image> imagenes = new ArrayList<Image>(); //Lista de imagenes (Image)

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DocumentReference docref = fStore.collection("Agenda1").document("Hoja1");

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


        //El capture que aparece acá debe ser si o si un png, ya que lo había
        //hecho con un xml y no funciona.

        //Opyions sirve para achicar ambas imágenes
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        //Esta es la imagen de la captura de foto, que al clickearla, deja la imagen atrás
        rotateImage = BitmapFactory.decodeResource(getResources(), R.drawable.capture, options);
        //Esta es la imagen que al clickearla rota la foto
        captureImage = BitmapFactory.decodeResource(getResources(), R.drawable.capture, options);

        Log.d(TAG, "MyCanvasView constructor: Antes de entrar a docreaf");



        docref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            Sheet hojaRecuperada = documentSnapshot.toObject(Sheet.class);
                            if(hojaRecuperada == null){
                                Log.d(TAG, "Docreaf: onEvent -> documentSnapshot.exists() -> if(hojaRecuperada == null)");
                            }
                            else {
                                Log.d(TAG, "Docreaf: onEvent -> documentSnapshot.exists() -> if(hojaRecuperada != null)");
                                setColorBackground(hojaRecuperada.backgroundColor);
                                if(hojaRecuperada.imageBitmap != null) {

                                    //Esto es para recuperar la imagen, lo hacemos a través de Picasso
                                    //Por alguna razón no se puede hacer en el Thread princial por lo que lo hacemos en uno secundario
                                    Thread thread = new Thread() {
                                        public void run() {

                                            try {
                                                toMove = true;
                                                bitmapRecuperado = Picasso.with(getContext())
                                                        .load(hojaRecuperada.imageBitmap.getImageUrl())
                                                        .get();
                                                image = Bitmap.createScaledBitmap(bitmapRecuperado, getWidth()/2, getHeight()/2,true);
                                                originalImage = image;
                                                postInvalidate();
                                                angle = hojaRecuperada.imageBitmap.getRotation();
                                                leftImage = hojaRecuperada.imageBitmap.getDimensionX();
                                                topImage = hojaRecuperada.imageBitmap.getDimensionY();
                                                refX = hojaRecuperada.imageBitmap.getPositionX();
                                                refY = hojaRecuperada.imageBitmap.getPositionY();

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    thread.start();
                                }




                                if(lines.isEmpty()){
                                    for(Stroke trazoRecuperado: hojaRecuperada.strokes){
                                        lines.add(trazoRecuperado);
                                        drawSegment(trazoRecuperado, paintFromColor( trazoRecuperado.getColor(), trazoRecuperado.getSize(), trazoRecuperado.getEraser()));
                                    }
                                }
                            }
                        }else{
                            Sheet sheet = new Sheet(colorBackground, lines);
                            //Solo tendríamos que updetear el stroke
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
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
        });
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

        //Si la imagen no es nula, y toMove es true, es decir se mueve, entonces;
        if(image != null && toMove){
            //Se encarga de dibujar la imagen que puse en setImage, la que saqué de drive
            //La redibuja si es que la roto
            drawImage(canvas);

            //Posición del centro de la imagen
            xCenter = leftImage + image.getWidth()/2 - captureImage.getWidth()/2;
            yCenter = topImage + image.getHeight()/2 - captureImage.getHeight()/2;

            //Posición esquina superior derecha de la imgen
            xRotate = leftImage + image.getWidth() + toPx(10);
            yRotate = topImage - toPx(10);

            //Dibujo la imagen que será el ícono para la rotación
            canvas.drawBitmap(rotateImage, xRotate, yRotate, null);
            //Dibujo la imagen que será el ícono para la copia de la imagen
            canvas.drawBitmap(captureImage, xCenter, yCenter, null);
        }

        //Bitmap de la vista completa
        canvas.drawBitmap(btnView, 0, 0, null );

    }

    private void drawImage(Canvas canvas) {
        Matrix matrix = new Matrix();
        //Hace la rotación de la imagen
        //Set the matrix to rotate by the specified number of degrees, with a pivot point at (px, py).
        matrix.setRotate(angle, image.getWidth()/2, image.getHeight()/2);
        //Posconcatena la matriz con la específica traslación,
        //básicamente este método translada la imagen
        matrix.postTranslate(leftImage, topImage);
        //drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint)
        //Draw the bitmap using the specified matrix.
        canvas.drawBitmap(image, matrix, null);
    }

    public void setColorBackground(int color){
        colorBackground = color;
        invalidate();
        docref.update("backgroundColor", colorBackground );
    }

    //Tamaño de la brocha
    public void setSizeBrush(int s){
        sizeBrush = s;
        //Con esto ajusta el tamaño de la brocha
        mPaint.setStrokeWidth(sizeBrush);
    }

    //Selecciona el color de la broche
    public void setBrushColor(int color){
        mPaint.setColor(color);
    }

    //Tamaño de la goma
    public void setSizeEraser(int s){
        sizeEraser = s;
        mPaint.setStrokeWidth(toPx(sizeEraser));
    }

    //Esta es la funcion que se usa para borrar
    public void enableEraser(){
        //activa la función borrar
        isEraser = true;
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void desableEraser(){
        isEraser = false;
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.setMaskFilter(null);
    }

    //Añade el nuevo bitmap a la lista
    public void addLastAction(Bitmap bitmap){
        listAction.add(bitmap);
    }

    //Retorna la última acción
    public void returnLastAction(){
        //Si hay algún elemento en la lista listAction que contiene los bitmaps (estos se añaden una vez se suelta el lapiz)
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
                touchStart(x,y);
                //Esta es la referencia de donde hice click
                refY = y;
                refX = x;

                if(image != null && toMove ){
                    //Si toco las esquinas de la imagen, entonces quiere decir que modificaré sus dimensiones
                    //Por lo tanto si lo tocado coincide con las esquinas de la imagen toResize es true
                    if(isToResize(refX, refY)) {
                        toResize = true;
                    //Sino coinciden, entonces no se modificarán sus dimensiones
                    }else{
                        toResize = false;
                    }
                    //Cuando clickeo la parte de la imagen de al centro, eso deja la imagen original atrás
                    //Y la nueva la puedo seguir moviendo
                    if((refX >= xCenter && refX < xCenter + captureImage.getWidth())
                    && (refY >= yCenter && refY < yCenter + captureImage.getHeight())){
                        //Crea una nueva imagen, pero la dibuja en el mismo canvas, no es un bitmap movible.
                        Canvas newCanvas = new Canvas(btmBackground);
                        drawImage(newCanvas);
                        invalidate();
                    }

                    //Cuando clickeo la parte de la imagen para rotar, a ángulo le añade 45 grados
                    if((refX >= xRotate && refX <= xRotate + rotateImage.getWidth())
                        && (refY >= yRotate && refY <= yRotate + rotateImage.getHeight())){
                        angle+=45;
                        invalidate();
                    }

                }

                break;
            case MotionEvent.ACTION_MOVE:
                //Si toMove es falso, entonces quiere decir que estoy dibujando un path
                if(!toMove){
                    touchMove(x,y);
                }
                //de lo contrario, pueden significar 2 cosas:
                //* O estoy modificando las dimensiones de la imagen
                //* O estoy moviendo la imagen
                else{
                    //returns position of the view you are touching
                    float nY = event.getY();
                    float nX = event.getX();

                    //Si toResize es true, quiere decir que en touch_down toqué las esquinas de la imagen
                    //Por lo que al moverme quiere decir que estoy modificando las dimensiones de la imagen
                    if(toResize){
                        //Nuevas dimensiones de la imagen
                        int xScale, yScale = 0;

                        //Si la posición que toco en la pantalla es mayor al de la imagen [horizontalmente]
                        if(nX > refX){
                            //Se suma esa referencia
                            xScale = (int) (image.getWidth() + (nX-refX));
                        //Si es menor
                        }else {
                            //Se resta
                            xScale = (int) (image.getWidth() - (refX - nX));
                        }
                        //Si la posición que toco en la pantalla es mayor al de la imagen [verticalmente]
                        if(nY > refY){
                            yScale = (int) (image.getHeight() + (nY-refY));
                        //Si es menor
                        }else {
                            //Se resta
                            yScale = (int) (image.getHeight() - (refY - nY));
                        }
                        //Luego resta ver si hubo un aumento recargar eso en el bitmap
                        //Ahora sale &&, no debería ser ||?
                        if (xScale > 0 && yScale > 0){
                            //A la imagen que subimos le cambiamos sus dimensiones
                            image = Bitmap.createScaledBitmap(originalImage, xScale, yScale, false);
                        }
                    //Sino se cambian las dimensiones:
                    }else {
                        //Las dimensiones de LeftImage aumentan la diferencia de donde estaba y estoy ahora
                        leftImage += nX - refX;
                        topImage += nY - refY;
                    }
                    //Solo me faltaba esto para mover bien la imagen
                    //Esto es para mover la imagen dentro del canvas
                    //La referencia de refx y refy cambian a lo que estoy tocando
                    refX = nX;
                    refY = nY;

                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if(!toMove){
                    touchUp();
                    //Cuando se suelta el dedo se añade el nuevo bitmap
                    //Todo: lo extraño que no entiendo es que en getbitmap solo guarda el lapiz
                    //Pero en el main al hacer get bitmap toma toodo, lapiz e imagenes
                    addLastAction(getBitmap());
                }
                break;
            default:
                //do nothing
        }

        return true;
    }

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
        if(pathCoordinates.size() ==1){
            Toast.makeText(getContext(), "Es un punto", Toast.LENGTH_SHORT).show();
            float x = pathCoordinates.get(0).x;
            float y = pathCoordinates.get(0).y;
            mX = pathCoordinates.get(0).x + 2;
            mY = pathCoordinates.get(0).y + 2;
            mPath.quadTo(x,y, (x+mX)/2, (y+mY)/2);
            mCanvas.drawPath(mPath, mPaint);
            //TODO no sé si este invalidate es necesario
            invalidate();

            //Guardar en el path
            Coordinates point = new Coordinates(mX, mY);
            pathCoordinates.add(point);
        }

        Stroke stroke = new Stroke(pathCoordinates, mPaint.getColor(), mPaint.getStrokeWidth(), isEraser);
        lines.add(stroke);

        Sheet sheet = new Sheet(colorBackground, lines);
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

    private void touchMove(float x, float y) {
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(x,y, (x+mX)/2, (y+mY)/2);

            mY = y;
            mX = x;
            //Dibujo el path en el canvas
            mCanvas.drawPath(mPath, mPaint);
            //TODO no sé si este invalidate es necesario
            invalidate();

            //Guardar en el path
            Coordinates point = new Coordinates(mX, mY);
            pathCoordinates.add(point);
        }
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
        mCanvas.drawPath(mPath, mPaint);
        //TODO no sé si este invalidate es necesario
        invalidate();


        //Inicializamos la lista de puntos:
        pathCoordinates = new ArrayList<Coordinates>();
        Coordinates point = new Coordinates(mX, mY);
        pathCoordinates.add(point);
    }

    //Esta funcion crea un nuevo bitmap con solo con el nuevo dibujo hecho al parecer
    public Bitmap getBitmap(){
        //Cuando la caché de dibujo está habilitada setDrawingCacheEnabled(true),
        //this se refiere a la vista: View, es decir la propia PaintView sino me equivoco
        // y setDrawingCacheEnabled(true) captura el contenido de la pantalla de android de esa vista
        this.setDrawingCacheEnabled(true);
        //la siguiente llamada a getDrawingCache()o buildDrawingCache()dibujará la vista en un mapa de bits
        this.buildDrawingCache();
        //Genera el bitmap de la vista en particular
        Bitmap bitmap = Bitmap.createBitmap(this.getDrawingCache());
        //Se desactiva el cache
        this.setDrawingCacheEnabled(false);
        return bitmap;
    }


    public void setImage(Bitmap bitmap, String name) {
        toMove = true;
        //Crea un bitmap de la imagen escogida
        // Returns a mutable bitmap with the specified width and height
        //El filtro lo único que hace es mejorar la imagen a costa del rendimiento (pero este costo es muy bajo, por lo que conviene)
        image = Bitmap.createScaledBitmap(bitmap, getWidth()/2, getHeight()/2,true);
        //A la originalImage le pasamos la imagen recién creada
        originalImage = image;
        invalidate();
        //Esto lo añadi yo addLastAction(getBitmap());
        //Funciona, la imagen creada se va, peeero cuando se borra toodo vuelve a aparecer
        //addLastAction(getBitmap());
        //Esto de acá es simplemente para crear una referencia del archivo
        StorageReference storageRefImages = storage.getReference("Images");
        StorageReference imageImagesRef = storageRefImages.child(name + ".jpg");

        //Para subir el archivo se hace lo siguiente:
        // Get the data from an ImageView as bytes

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataImage = baos.toByteArray();


        final Uri[] uriImage = {null};
        UploadTask uploadTask = imageImagesRef.putBytes(dataImage);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Error: "+ exception, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageImagesRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getContext(), "Error al obtener el link de descarga", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(getContext(), "Uri: "+ uri, Toast.LENGTH_SHORT).show();
                        Image imagen = new Image(name, uri.toString());
                        Log.d(TAG, "drawSegment");
                        //imagen.setImageUri(uri);
                        imagen.setDimensionX(image.getWidth() / 2);
                        imagen.setDimensionY(image.getHeight() / 2);
                        imagen.setPositionX(leftImage);
                        imagen.setPositionY(topImage);
                        imagen.setRotation(angle);
                        docref.update("imageBitmap", imagen);
                    }
                });

                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Toast.makeText(getContext(), "archivo subido a FireStore", Toast.LENGTH_SHORT).show();

            }
        });

        //String ueii = imageImagesRef.getDownloadUrl()/*.getResult()*/.toString();
        if(uriImage[0] != null) {
            /*
            Image imagen = new Image(name, uriImage[0].toString());
            Log.d(TAG, "drawSegment");
            imagen.setImageUri(uriImage[0]);
            imagen.setDimensionX(image.getWidth() / 2);
            imagen.setDimensionY(image.getHeight() / 2);
            imagen.setPositionX(leftImage);
            imagen.setPositionY(topImage);
            imagen.setRotation(angle);
            docref.update("imageBitmap", imagen);

             */
        }else{
            Toast.makeText(getContext(), "Algo paso que el link de descarga es null", Toast.LENGTH_SHORT).show();
        }



    }

    /*Los siguientes métodos son para recuperar los puntos cuando hubo una rotación*/

    private void drawSegment(Stroke stroke, Paint paint) {
        //Si el canvas mBuffer es distinto de null, dibuja un Path
        //Basicamente si hay canvas dibuja el path con el paint
        Log.d(TAG, "drawSegment");
        if (mCanvas != null) {
            mCanvas.drawPath(getPathForPoints(stroke.getPoints()), paint);
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
    public static Paint paintFromColor(int color, float width, boolean isEnableEraser) {
        Log.d(TAG, "Paint paintFromColor 1");
        return paintFromColor(color, Paint.Style.STROKE, width, isEnableEraser);
    }

    //Retorna lo mismo que el anterior, pero ahora se le pasa un estilo diferente
    public static Paint paintFromColor(int color, Paint.Style style, float width, boolean isEnableEraser) {
        Log.d(TAG, "Paint paintFromColor 2");
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(width);
        if(isEnableEraser){
            setEraserEnable(p);
        }
        else {
            setEraserDisable(p);
        }
        return p;
    }

    public static void setEraserEnable(Paint mPaint){
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public static void setEraserDisable(Paint mPaint){
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.setMaskFilter(null);
    }

}

