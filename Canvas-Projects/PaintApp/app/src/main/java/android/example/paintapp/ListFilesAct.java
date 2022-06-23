package android.example.paintapp;

import android.example.paintapp.adabters.FilesAdabters;
import android.example.paintapp.common.Common;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListFilesAct extends AppCompatActivity {

    //Tengo una lista de archivos
    List<File> fileList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Es el recicler de las imágenes
        setContentView(R.layout.activity_list_files);

        initToolbar();
        initView();
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_files);
        recyclerView.setHasFixedSize(true);
        //Vista de Grilla
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        //Retorna las imágenes guardadas en el directorio Environment.DIRECTORY_PICTURES
        fileList = loadFile();
        //Creamos un nuevo adaptador FilesAdabters y le pasamos la lista fileList
        FilesAdabters filesAdabters = new FilesAdabters(this, fileList);
        //Y al recycler le pasamos el adaptador
        recyclerView.setAdapter(filesAdabters);
    }

    //Método que retorna una lista de archivos
    private List<File> loadFile() {
        //Creamos una nueva lista vacía
        ArrayList<File> inFiles = new ArrayList<>();
        File parendDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+ getString(R.string.app_name));

        File[] files  = parendDir.listFiles();

        if(files!=null){
            //Recorremos los files
            for (File file: files){

                if(file.getName().endsWith(".png"))
                    inFiles.add(file);
            }
        }else{
            Toast.makeText(this, "No hay archivos", Toast.LENGTH_SHORT).show();
        }

        //Texto que te señala si hay imágenes o no
        TextView textView = findViewById(R.id.status_empty);
        //Si existe al menos un archivo el texto se saca
        if(files.length>0){
            textView.setVisibility(View.GONE);
        }
        //Sino se hace visible
        else {
            textView.setVisibility(View.VISIBLE);
        }
        //Retorna los archivos
        return inFiles;
    }

    //Da el diseño del toobal
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pictures");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){ //Basicamente se apreto la flecha del navbar me voy para atrás
            finish();
        }
        return true;
    }

    //Al mantener apretada la imagen aparece un menu
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //Y ese menu tiene el botón elimina
        if(item.getTitle().equals(Common.DELETE)){
            deleteFile(item.getOrder());
            initView();
        }
        return true;
    }

    //Y puedo eliminar el archivo si lo selecciono.
    private void deleteFile(int order) {
        fileList.get(order).delete();
        fileList.remove(order);
    }
}