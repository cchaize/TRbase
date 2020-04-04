package com.chaize.tr.controleur;

import android.content.Context;

import com.chaize.tr.modele.AccesDistant;
import com.chaize.tr.modele.AccesLocal;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.modele.Shop;
import com.chaize.tr.outils.Serializer;
import com.chaize.tr.vue.ItemShop;
import com.chaize.tr.vue.MajActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class Controle {

    public static final String EXTRA_PRODUIT = "PRODUIT";
    private static Controle instance = null;
    private static Produit produit;
    private ArrayList<ItemShop> arrayListMagasins = new ArrayList<>();
    private static String nomfic = "saveProduct";
    private static AccesLocal accesLocal;
    private static AccesDistant accesDistant;
    private static Context contexte;
    private static Shop magasin;
    private static ArrayList<String> log = new ArrayList<>();

    // Codes retour des Activity
    public static final int SEL_MAGASIN = 2;
    public static final int SEL_MAGASIN_OK = 3;
    public static final int MAJ_PRODUIT_FROM_PANIER = 4;
    public static final int MAJ_PRODUIT_FROM_LISTE = 5;

    public enum typeLog {
        INFO, ERROR
    }

    private Controle() {
        super();
    }

    /**
     * Création de l'instance
     *
     * @return instance
     */
    public static final Controle getInstance(Context contexte) {
        if (contexte != null) {
            Controle.contexte = contexte;
        }
        if (Controle.instance == null) {
            Controle.instance = new Controle();
            accesLocal = new AccesLocal(contexte);
            accesDistant = new AccesDistant();
            accesDistant.envoi("listeMagasins", null);
            int seqMagasin = 0;
            magasin = null;
            try {
                seqMagasin = Integer.parseInt(accesLocal.getParam("magasin"));
                magasin = accesLocal.getMagasin(seqMagasin);
            } catch (NumberFormatException e) {
            }
            if (magasin == null)
                magasin = new Shop(0, "Pas de magasin défini", 00000, "");

        }
        return Controle.instance;
    }


    public void chercheProduit(String code) throws TRexception {
        //recupSerialize(contexte);
        //produit = accesLocal.recupProduit(code);
        produit = null;

        if (this.magasin.getSequence() == 0)
            throw new TRexception(1, TRexception.getMessage(1));

        List param = new ArrayList();
        if (accesDistant.isAvailable()) {
            param.add(code);
            param.add(this.magasin.getSequence());
            accesDistant.envoi("lecture", new JSONArray(param));
        } else {

            try {
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("code", code);
                jsonParam.put("magasin", this.magasin.getSequence());
                accesLocal.envoi("lecture", new JSONArray().put(jsonParam));
            } catch (JSONException e) {
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "Controle.chercheProduit : " + e.toString());
            }
        }
    }

    public ArrayList getListeMagasins(int cPostal) {
        return accesLocal.getListeMagasins(cPostal);
    }

    public void setProduit(Produit produit) {
        Controle.produit = produit;
        ((MajActivity) contexte).afficheProduit();
    }


    public void enregProduit(String code, String description, Integer flgTR, Context contexte) {
        produit = new Produit(code, this.magasin.getSequence(), description, flgTR, 0.0f, 0);
        if (accesDistant.isAvailable())
            accesDistant.envoi("enreg", produit.convert2JSONArray());
        else
            accesLocal.enregProduit(produit);
    }

    public Integer getFlgTR() {
        if (produit == null)
            return 0;
        return produit.getFlgTR();
    }

    public Float getPrix() {
        if (produit == null)
            return 0.0f;
        return produit.getPrix();
    }

    /**
     * Récupération de l'objet sérialisé (Produit)
     *
     * @param contexte
     */
    //private static void recupSerialize(Context contexte){
    public static void recupSerialize(Context contexte) {
        produit = (Produit) Serializer.deserialize(nomfic, contexte);
    }

    public String getCode() {
        if (produit == null)
            return "**************";
        return produit.getCode();
    }

    public Shop getMagasin() {
        return magasin;
    }

    public void setMagasin(Shop magasin) {
        Controle.magasin = magasin;
        accesLocal.enregParam("magasin", String.valueOf(magasin.getSequence()));
    }

    public String getDescription() {
        if (produit == null)
            return "inconnu";
        return produit.getDescription();
    }

    public static void addLog(typeLog type, String msg) {
        log.add(type.toString() + " --> " + msg);
        // On ne conserve que les 10 derniers messages
        while (log.size() > 10) {
            log.remove(0);
        }
    }

    public static String getLog() {
        String ret = "Log Controleur:\n";
        int i = 0;
        while (i < log.size()) {
            ret += log.get(i) + "\n";
            i++;
        }
        return ret;
    }

}
