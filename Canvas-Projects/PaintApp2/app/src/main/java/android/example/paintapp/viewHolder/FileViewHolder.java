package android.example.paintapp.viewHolder;

import android.example.paintapp.Interface.ViewOnClick;
import android.example.paintapp.R;
import android.example.paintapp.common.Common;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class FileViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    private ViewOnClick viewOnClick;

    //Hace que este elemento sea clickeable
    public void setViewOnClick(ViewOnClick viewOnClick){
        this.viewOnClick = viewOnClick;
    }

    public FileViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);

        //Toma el image de row_files
        imageView = itemView.findViewById(R.id.image);
        //Y hacemos toda esta imagen clickeable
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnClick.onClick(getAdapterPosition());
            }
        });

        itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0,0,getAdapterPosition(), Common.DELETE);
            }
        });
    }
}

