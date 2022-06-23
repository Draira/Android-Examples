package android.example.paintapp.adabters;

import android.content.Context;
import android.content.Intent;
import android.example.paintapp.Interface.ViewOnClick;
import android.example.paintapp.R;
import android.example.paintapp.ViewFileAct;
import android.example.paintapp.viewHolder.FileViewHolder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class FilesAdabters extends RecyclerView.Adapter<FileViewHolder> {

    private Context mContext;
    private List<File> fileList;

    //Recibe un contexto y una lista de Files
    public FilesAdabters(Context mContext, List<File> fileList){
        this.mContext = mContext;
        this.fileList = fileList;
    }
    @NonNull
    @NotNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //Infla la vista de row_files ( que tiene un pequeño image View)
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_files, parent, false);

        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FileViewHolder holder, int position) {
        holder.imageView.setImageURI(Uri.fromFile(fileList.get(position)));

        //Aca es cuando yo clickeo una de las imagenes me lleva a una vista completa de la imagen:
        holder.setViewOnClick(new ViewOnClick() {
            @Override
            public void onClick(int pos) {
                Intent intent = new Intent(mContext, ViewFileAct.class);
                //Envía la imagen clickeada a ViewFileAct
                intent.setData(Uri.fromFile(fileList.get(pos)));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
}
