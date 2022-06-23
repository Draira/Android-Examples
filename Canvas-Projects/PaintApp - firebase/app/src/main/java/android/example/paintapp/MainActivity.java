package android.example.paintapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.example.paintapp.Interface.ToolsListener;
import android.example.paintapp.adabters.ToolsAdabters;
import android.example.paintapp.common.Common;
import android.example.paintapp.model.Image;
import android.example.paintapp.model.ToolsItem;
import android.example.paintapp.widget.PaintView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ToolsListener {

    private static final String TAG = "MainActivity";
    //Botones de la barra inferior
    ImageButton back;
    ImageButton share;
    ImageButton home;
    ImageButton download;

    private static final int REQUEST_PERMISSSION = 1001;
    private static final int PICK_IMAGE = 1000;

    //Para guardar el bitmap en FirebaseStorage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference docref = fStore.collection("Agenda1").document("Hoja1");

    //Vista paint
    PaintView mPaintView;
    int colorBackground, colorBrush;
    int brushSize, eraserSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initTools();
    }

    private void initTools() {
        colorBackground = Color.BLUE;
        colorBrush = Color.BLACK;
        eraserSize = brushSize = 12;
        mPaintView = findViewById(R.id.paint_view);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        ToolsAdabters toolsAdabters = new ToolsAdabters(loadTools(), this);
        recyclerView.setAdapter(toolsAdabters);

        //Agrego los botones de la vista
        back = findViewById(R.id.botton_arrow_back);
        share = findViewById(R.id.botton_share);
        home = findViewById(R.id.botton_home);
        download = findViewById(R.id.botton_download);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishPaint(v);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareApp(v);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFiles(v);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                saveFile(v);
            }
        });
    }

    public void finishPaint(View view){
        Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show();
    }

    public void shareApp(View view){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String bodyText = "http://play.google.com/store/apps/details?id="+this.getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, bodyText);
        startActivity(Intent.createChooser(intent, "share this app"));
    }

    //Basicamente al apretar en la casa nos envía a la vista cuadrillé
    public void showFiles(View view){
        //Al apretar en showFiles nos envía a ListFilesAct
        Intent intent = new Intent(this, ListFilesAct.class);
        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void saveFile(View view){
        //Revisa si tiene permisos, sino los pide
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            //requestPermissions esta deprecado
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSSION);
        }
        //Si los tiene guarda el bitmap
        else{
            //
            saveBitmap();
        }
    }

    private void saveBitmap() {
        //Toma el bitmap de PaintView
        Bitmap bitmap = mPaintView.getBitmap();
        //Basicamente lo convierte en imagen
        String file_name = UUID.randomUUID() + ".png";

        File folder = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+getString(R.string.app_name));
        //Si esta carpeta no existe, la crea
        if(!folder.exists()){
            folder.mkdirs();
        }
        //Intenta guardar la imagen
        //Creo que es un directorio interno dentro de la App
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(folder+File.separator+file_name);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100,  fileOutputStream);
            Toast.makeText(this, "picture saved", Toast.LENGTH_SHORT).show();

        //Sino entrega mensaje de error:
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        //Además salvaremos el bitmap en firestore yaas, para eso usarmos FirebaseStorange

        //Esto de acá es simplemente para crear una referencia del archivo
        StorageReference storageRef = storage.getReference();
        StorageReference mountainsRef = storageRef.child("mountains.jpg");
        StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

        //Para subir el archivo se hace lo siguiente:
        // Get the data from an ImageView as bytes

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this, "Error: "+ exception, Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Toast.makeText(MainActivity.this, "archivo subido a FireStore", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSSION && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveBitmap();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private List<ToolsItem> loadTools() {
        //Creo una lista de ToolsItem (clase java en model -> ToolsItem
        List<ToolsItem> result = new ArrayList<>();
        result.add(new ToolsItem(R.drawable.brush_24, Common.BRUSH));
        result.add(new ToolsItem(R.drawable.eraser_24,Common.ERASER));
        result.add(new ToolsItem(R.drawable.image_24,Common.IMAGE));
        result.add(new ToolsItem(R.drawable.palette_24,Common.COLORS));
        result.add(new ToolsItem(R.drawable.paint_24,Common.BACKGROUND));
        result.add(new ToolsItem(R.drawable.undo_24,Common.RETURN));
        return result;
    }

    @Override
    public void onSelected(String name) {
        switch (name){
            case Common.BRUSH:
                mPaintView.toMove = false;
                mPaintView.desableEraser();
                mPaintView.invalidate();
                showDialogSize(false);
                break;
            case Common.ERASER:
                mPaintView.enableEraser();
                showDialogSize(true);
                break;

            case Common.RETURN:
                mPaintView.returnLastAction();
                break;
            case Common.BACKGROUND:
                updateColor(name);
                break;
            case Common.COLORS:
                updateColor(name);
                break;
            case Common.IMAGE:
                getImage();
        }

    }
    //Selecciona una imagen
    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if(requestCode == PICK_IMAGE && data != null && resultCode == Activity.RESULT_OK){
            //Toma una imagen de todas las que hay
            Uri pickedImage = data.getData();
            String name = new File(pickedImage.getPath()).getName();

            //File f = new File("" + pickedImage);
            //String nameImage = f.getName();
            Log.d(TAG, "nameImage: " + name);
            InputStream is = null;

            try {
                //Toma la imagen escogida como un InputStream
                is = this.getContentResolver().openInputStream(pickedImage);


                //BitmapFactory.Options es para escalar la imagen o darle características
                BitmapFactory.Options options = new BitmapFactory.Options();
                //la característicaARGB_8888 Each pixel is stored on 4 bytes.
                // Each channel (RGB and alpha for translucency) is stored with 8 bits of precision (256 possible values.)
                // This configuration is very flexible and offers the best quality. It should be used whenever possible.
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                //Ahora transforma la imgen en bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //Y manda esta imagen al PaintView
                mPaintView.setImage(bitmap, name);
                is.close();




            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "FileNotFoundException" +e, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "IOException" +e, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateColor(String name){
        int color;

        if(name.equals(Common.BACKGROUND)){
            color = colorBackground;
        }
        else{
            color = colorBrush;
        }

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        if(name.equals(Common.BACKGROUND)){
                            colorBackground = lastSelectedColor;
                            mPaintView.setColorBackground(colorBackground);
                        }
                        else {
                            colorBrush = lastSelectedColor;
                            mPaintView.setBrushColor(colorBrush);
                        }
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).build()
                .show();


    }

    private void showDialogSize(boolean isEreser){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null, false);

        TextView toolsSelected = view.findViewById(R.id.status_tools_selected);
        TextView statusSize = view.findViewById(R.id.status_size);
        ImageView ivTools = view.findViewById(R.id.iv_tools);
        SeekBar seekBar = view.findViewById(R.id.seekbar_size);
        seekBar.setMax(99);

        if(isEreser){
            toolsSelected.setText("Eraser Size");
            ivTools.setImageResource(R.drawable.eraser_black_24);
            statusSize.setText("Selected Size: " +eraserSize);
        }
        else{
            toolsSelected.setText("Brush size");
            ivTools.setImageResource(R.drawable.brush_black_24);
            statusSize.setText("Selected Size: "+brushSize);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isEreser){
                    eraserSize = progress + 1;
                    statusSize.setText("Selected Size: " + eraserSize);
                    mPaintView.setSizeEraser(eraserSize);
                }
                else {
                    brushSize = progress + 1;
                    statusSize.setText("Selected Size: " + brushSize);
                    mPaintView.setSizeBrush(brushSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setView(view);
        builder.show();
    }

}