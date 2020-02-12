package com.chaize.tr.modele;

import com.chaize.tr.controleur.Controle;

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
    private Integer syncStatus;

    public Produit(String code, Integer magasin, String description, Integer flgTR, Integer syncStatus) {
        this.setCode(code);
        this.setMagasin(magasin);
        this.setDescription(description);
        this.setFlgTR(flgTR);
        this.setSyncStatus(syncStatus);
    }

    public Produit(JSONObject row) {
        try {
            this.setCode(row.getString("code"));
            this.setDescription(row.getString("description"));
            this.setFlgTR(row.getInt("flgTR"));
            if (row.has("magasin"))
                this.setMagasin(row.getInt("magasin"));
            if (row.has("syncStatus"))
                this.setSyncStatus(row.getInt("syncStatus"));
            else
                this.setSyncStatus(0);
        } catch (JSONException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR,e.toString());
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

    public static Integer getFlgTRfromBase(String code) {
        return (int) (Math.random()*3);
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
