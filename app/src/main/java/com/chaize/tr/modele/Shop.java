package com.chaize.tr.modele;

import com.chaize.tr.controleur.Controle;

import org.json.JSONException;
import org.json.JSONObject;

public class Shop {
    private int sequence;
    private String nom;
    private int codePostal;
    private String ville;

    public Shop(int sequence, String nom, int codePostal, String ville) {
        this.sequence = sequence;
        this.nom = nom;
        this.codePostal = codePostal;
        this.ville = ville;
    }

    public Shop(JSONObject jsonShop) {
        try {
            this.sequence = jsonShop.getInt("sequence");
            this.nom = jsonShop.getString("nom");
            this.codePostal = jsonShop.getInt("code_postal");
            this.ville = jsonShop.getString("ville");
        } catch (JSONException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR,e.toString());
        }

    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(int codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}
