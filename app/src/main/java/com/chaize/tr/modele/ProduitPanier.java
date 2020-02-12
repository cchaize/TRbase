package com.chaize.tr.modele;

public class ProduitPanier {
    private float quantite=0;
    private Produit produit=null;

    public ProduitPanier(float quantite, Produit produit) {
        this.quantite = quantite;
        this.produit = produit;
    }
}
