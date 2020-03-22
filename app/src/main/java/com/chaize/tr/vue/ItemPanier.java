package com.chaize.tr.vue;

import com.chaize.tr.modele.Produit;

public class ItemPanier {
    private Produit produit;
    private float quantite;
    private float prix;

    public ItemPanier(Produit produit, float quantite, float prix) {
        this.produit = produit;
        this.quantite = quantite;
        this.prix = prix;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public float getQuantite() {
        return quantite;
    }

    public void setQuantite(float quantite) {
        this.quantite = quantite;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }
}
