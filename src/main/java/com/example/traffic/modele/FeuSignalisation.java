package com.example.traffic.modele;

public class FeuSignalisation {

    private boolean vert = false;
    private long lastChange = System.currentTimeMillis();

    public void mettreAJour(int trafic) {

        long now = System.currentTimeMillis();

        long duree = 3000 + trafic * 300;
        if (duree > 8000) duree = 8000;

        if (now - lastChange > duree) {
            vert = !vert;
            lastChange = now;
        }
    }

    public boolean estVert() {
        return vert;
    }
}