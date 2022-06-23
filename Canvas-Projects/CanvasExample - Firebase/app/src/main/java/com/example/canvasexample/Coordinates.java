package com.example.canvasexample;

//Coordenadas de los trazos
public class Coordinates {
    float x;
    float y;

    //Constructor vacío necesario para firebase
    public Coordinates() {
    }

    //Constructor para crear un nuevo Coordinates
    public Coordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }

    //SETTERS & GETTERS
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
