package android.example.paintapp.viewHolder;

import android.example.paintapp.Interface.ViewOnClick;
import android.example.paintapp.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ToolsViewHolder extends RecyclerView.ViewHolder {

    /*
    Por resumirlo de alguna forma, un view holder se encargará de contener y gestionar las vistas
    o controles asociados a cada elemento individual de la lista.
    El control RecyclerView se encargará de crear tantos view holder como sea necesario para
    mostrar los elementos de la lista que se ven en pantalla y los gestionará eficientemente de forma
    que no tenga que crear nuevos objetos para mostrar más elementos de la lista al hacer scroll,
    sino que tratará de «reciclar» aquellos que ya no sirven por estar asociados a otros elementos de la
    lista que ya han salido de la pantalla.
     */

    public ImageView icone;
    public TextView name;

    private ViewOnClick viewOnClick;

    public void setViewOnClick(ViewOnClick viewOnClick) {
        //this hace referencia a ToolsViewHolder
        this.viewOnClick = viewOnClick;
    }

    public ToolsViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
        super(itemView);

        icone = itemView.findViewById(R.id.tools_icon);
        name = itemView.findViewById(R.id.tools_name);

        //Toma to_do el elemento para hacerlo clickeable
        //Crea así el paquete  Interface y crea la clase ViewOnClick
        //Usando así la línea private ViewOnClick viewOnClick;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnClick.onClick(getAdapterPosition());

            }
        });
    }
}
