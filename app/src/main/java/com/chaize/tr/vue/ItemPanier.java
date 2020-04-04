package com.chaize.tr.vue;

import com.chaize.tr.modele.Produit;

public class ItemPanier {
    private Produit produit;
    private int quantite;
    private float prix;
    private int position;

    public ItemPanier(Produit produit, int quantite, float prix, int position) {
        this.produit = produit;
        this.quantite = quantite;
        this.prix = prix;
        this.position = position;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
