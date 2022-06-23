package android.example.paintapp.model;

import java.util.ArrayList;
import java.util.List;

public class Sheet {
    public int backgroundColor;
    public List<Stroke> strokes = new ArrayList<Stroke>();
    //Imagenes dejadas en el canvas
    public List<Image> images = new ArrayList<Image>();
    //Imagen del bitmap
    public Image imageBitmap;

    public Sheet() {
    }

    public Sheet(int backgroundColor, List<Stroke> strokes) {
        this.backgroundColor = backgroundColor;
        this.strokes = strokes;
    }

    public void addStroke(List<Coordinates> points, int color, int size, boolean isEraser) {
        Stroke p = new Stroke(points, color, size, isEraser);
        strokes.add(p);
    }
}
