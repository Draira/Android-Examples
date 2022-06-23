package android.example.paintapp.model;

import java.util.ArrayList;
import java.util.List;

public class Stroke {
    //Guardo una lista con todos la rayita hecha
    List<Coordinates> points = new ArrayList<Coordinates>();
    //La raya tiene un color
    int color;
    //Y la raya también tiene un grosor
    float size;
    //Si es goma o stroke
    Boolean isEraser;

    public Stroke() {
    }

    /*Creo que tienen que ser así, porque puede cambiar al siguiente stroke*/
    public Stroke(List<Coordinates> points, int color, float size, boolean isEraser) {
        this.points = points;
        this.color = color;
        this.size = size;
        this.isEraser = isEraser;
    }

    public List<Coordinates> getPoints() {
        return points;
    }

    public void setPoints(List<Coordinates> points) {
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Boolean getEraser() {
        return isEraser;
    }

    public void setEraser(Boolean eraser) {
        isEraser = eraser;
    }


}
