package com.example.traffic.modele;

public class Vehicule {

    private double x;
    private double y;
    private double vitesse;
    private boolean arrete;
    private boolean horizontal;

    public Vehicule(double x, double y, double vitesse, boolean horizontal) {
        this.x = x;
        this.y = y;
        this.vitesse = vitesse;
        this.horizontal = horizontal;
    }

    public void avancerHorizontal() {
        if (!arrete) x += vitesse;
    }

    public void avancerVertical() {
        if (!arrete) y += vitesse;
    }

    public void arreter() {
        arrete = true;
    }

    public void demarrer() {
        arrete = false;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public double getPositionX() {
        return x;
    }

    public double getPositionY() {
        return y;
    }
}