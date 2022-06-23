package android.example.paintapp.adabters;

import android.example.paintapp.Interface.ToolsListener;
import android.example.paintapp.Interface.ViewOnClick;
import android.example.paintapp.R;
import android.example.paintapp.model.ToolsItem;
import android.example.paintapp.viewHolder.ToolsViewHolder;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ToolsAdabters extends RecyclerView.Adapter<ToolsViewHolder> {

    private List<ToolsItem> toolsItemsList;
    private int selected = -1;
    //En el recycler view tienes un listener
    private ToolsListener listener;

    public ToolsAdabters(List<ToolsItem> toolsItemsList, ToolsListener listener) {
        this.toolsItemsList = toolsItemsList;
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public ToolsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tools_item, parent, false);
        return new ToolsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ToolsViewHolder holder, int position) {
        holder.name.setText(toolsItemsList.get(position).getName());
        holder.icone.setImageResource(toolsItemsList.get(position).getIcone());

        holder.setViewOnClick(new ViewOnClick() {
            @Override
            public void onClick(int pos) {
                selected = pos;
                listener.onSelected(toolsItemsList.get(pos).getName());
                notifyDataSetChanged();
            }
        });

        //Hace que la letra seleccionada se marque por sobre las otras
        if(selected == position){
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD);
        }else{
            holder.name.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return toolsItemsList.size();
    }
}
