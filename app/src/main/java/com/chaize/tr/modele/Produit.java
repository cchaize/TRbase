package com.chaize.tr.modele;

import com.chaize.tr.controleur.Controle;
import com.chaize.tr.outils.DbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Produit implements Serializable {
    private String code;
    private Integer magasin;
    private String description;
    private Integer flgTR; // 0=inconnu 1=Non Eligible 2=Eligible TR
    private Float prix;
    private Integer syncStatus;

    public Produit(String code, Integer magasin) {
        this(code, magasin,"inconnu", DbContract.TR_INCONNU, 0.0f, DbContract.SYNC_STATUS_FAILED);
    }

    public Produit(String code, Integer magasin, String description, Integer flgTR, Float prix, Integer syncStatus) {
        this.setCode(code);
        this.setMagasin(magasin);
        this.setDescription(description);
        this.setFlgTR(flgTR);
        this.setPrix(prix);
        this.setSyncStatus(syncStatus);
    }

    public Produit(JSONObject row) {
            try {
                this.setCode(row.getString("code"));
                this.setDescription(row.getString("description"));
                this.setFlgTR(row.getInt("flgTR"));
                this.setPrix(new Float(row.getDouble("prix")));
                if (row.has("magasin"))
                    this.setMagasin(row.getInt("magasin"));
                if (row.has("syncStatus"))
                    this.setSyncStatus(row.getInt("syncStatus"));
                else
                    this.setSyncStatus(DbContract.SYNC_STATUS_FAILED);
            } catch (JSONException e) {
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
            }
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMagasin(Integer magasin) {
        this.magasin = magasin;
    }

    public void setFlgTR(Integer flgTR) {
        if (flgTR>=0 && flgTR<3)
            this.flgTR = flgTR;
        else
            this.flgTR = 0;
    }

    public String getCode() {
        return code;
    }

    public Integer getMagasin() {
        return magasin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFlgTR() {
        return Math.min(2,Math.max(0,flgTR));
    }

    public Float getPrix() {
        return prix;
    }

    public void setPrix(Float prix) {
        this.prix = prix;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * conversion du produit au format JSON
     * @return
     */
    public JSONArray convert2JSONArray() {
        List laListe = new ArrayList();
        laListe.add(code);
        laListe.add(magasin);
        laListe.add(description);
        laListe.add(flgTR);
        return new JSONArray(laListe);
    }

}
