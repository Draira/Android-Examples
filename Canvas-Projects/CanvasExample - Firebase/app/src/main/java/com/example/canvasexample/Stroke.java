package com.example.canvasexample;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Stroke {
    List<Coordinates> points = new ArrayList<Coordinates>(); //Lista de puntos del trazo
    int color; //Color del trazo
    float size; //Grosor del trazo

    public Stroke() {//Constructor vacío necesario para firebase
    }

    //Constructor para crear un nuevo Stroke
    public Stroke(List<Coordinates> points, int color, float size) {
        this.points = points;
        this.color = color;
        this.size = size;
    }

    //SETTTERS & GETTERS
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

    public void setSize(int size) {
        this.size = size;
    }
}
