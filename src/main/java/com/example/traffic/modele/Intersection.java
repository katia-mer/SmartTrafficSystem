package com.example.traffic.modele;

import java.util.ArrayList;
import java.util.List;

public class Intersection {

    private FeuSignalisation feu = new FeuSignalisation();
    private List<Vehicule> vehicules = new ArrayList<>();

    private final double LIGNE_X = 350;
    private final double LIGNE_Y = 150;

    private int voituresPassees = 0;
    private long startTime = System.currentTimeMillis();

    public void ajouterVehicule(Vehicule v) {
        vehicules.add(v);
    }

    public void mettreAJour() {

        feu.mettreAJour(vehicules.size());

        boolean vert = feu.estVert();

        for (Vehicule v : vehicules) {

            if (v.isHorizontal()) {

                if (!vert && v.getPositionX() >= LIGNE_X - 20) {
                    v.arreter();
                } else {
                    v.demarrer();
                }

                v.avancerHorizontal();

            } else {

                if (vert && v.getPositionY() >= LIGNE_Y - 20) {
                    v.arreter();
                } else {
                    v.demarrer();
                }

                v.avancerVertical();
            }

            if (v.getPositionX() > 800 || v.getPositionY() > 400) {
                voituresPassees++;
            }
        }
    }

    public List<Vehicule> getVehicules() {
        return vehicules;
    }

    public FeuSignalisation getFeu() {
        return feu;
    }

    public int getNombreVehicules() {
        return vehicules.size();
    }

    public int getVoituresPassees() {
        return voituresPassees;
    }

    public long getTempsEcoule() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}