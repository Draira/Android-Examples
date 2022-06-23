package com.example.canvasexample;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Sheet {
    public int backgroundColor;
    public List<Stroke> strokes = new ArrayList<Stroke>();

    //Constructor necesario para firebase
    public Sheet() {
    }

    public Sheet(int backgroundColor, List<Stroke> strokes) {
        this.backgroundColor = backgroundColor;
        this.strokes = strokes;
    }

    public void addStroke(List<Coordinates> points, int color, int size) {
        Stroke p = new Stroke(points, color, size);
        strokes.add(p);
    }



}
