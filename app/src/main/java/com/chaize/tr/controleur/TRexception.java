package com.chaize.tr.controleur;

public class TRexception extends Exception {

    public Integer code;    // code erreur

    public TRexception(Integer code, String mes) {
        super(mes);
        this.code = code;
    }

    public static String getMessage(Integer code) {
        String mes;
        switch (code) {
            case 1 : mes = "Magasin non sélectionné"; break;
            default : mes = "Erreur inconnue"; break;
        }
        return mes;
    }
}
