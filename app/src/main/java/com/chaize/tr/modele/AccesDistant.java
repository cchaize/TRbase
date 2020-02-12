package com.chaize.tr.modele;

import android.util.Log;

import com.chaize.tr.controleur.Controle;
import com.chaize.tr.outils.AccesHTTP;
import com.chaize.tr.outils.AsyncResponse;
import com.chaize.tr.vue.ItemShop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccesDistant implements AsyncResponse {

    // constante
    private static final String SERVERADDR = "http://cyril.chaize.free.fr/baseTR/serveurBaseTR.php";
    //private static final String SERVERADDR = "http://cchaize.000webhostapp.com/TRbase/serveurBaseTR.php";
    //private static final String SERVERADDR = "http://basetr.yj.fr/serveurBaseTR.php";

    ////////////////////////////////////////////
    // Voir https://fr.openfoodfacts.org/data pour obtenir les désignations et images des codes barres
    ////////////////////////////////////////////

    private Controle controle;

    public AccesDistant(){
        controle = Controle.getInstance(null);
    }

    /**
     * Teste si la connexion distante est possible
     * @return true/false
     */
    public boolean isAvailable(){
        // Tester si connexion est disponible
        return false;
    }

    /**
     * retour du serveur distant
     * @param output
     */
    @Override
    public void processFinish(String output) {
        Log.d("serveur", "******************* "+output);
        //découpage du message reçu avec %
        String[] message = output.split("%");
        // dans message[0] : "enreg", "lecture", "Erreur !", "listeMagasins"
        // dans message[1] : reste du message

        // s'il y a 2 cases
        if (message.length>1){
            if (message[0].equals("enreg")){
                Log.d("enreg", "******************* "+message[1]);
            } else if (message[0].equals("lecture")){
                Log.d("lecture", "******************* "+message[1]);
                try {
                    Produit produit;
                    if (message[1].equals("notfound")) {
                        produit = new Produit(message[2], controle.getMagasin().getSequence(), "inconnu", 0, 1);
                    } else {
                        JSONObject info = new JSONObject(message[1]);
                        String code = info.getString("code");
                        String desc = info.getString("description");
                        Integer flgtr = info.getInt("flgTR");

                        produit = new Produit(code, controle.getMagasin().getSequence(), desc, flgtr, 1);
                    }
                    controle.setProduit(produit);

                } catch (JSONException e) {
                    Log.d("Erreur", "conversion JSON impossible "+e.toString());
                }
            } else if (message[0].equals("listeMagasins")){
                Log.d("listeMagasins", "******************* "+message[1]);

                try {
                    JSONObject info = new JSONObject(message[1]);
                    Log.d("listeMagasins", "#############******************* "+info.toString());
                } catch (JSONException e) {
                    Log.d("Erreur", "conversion JSON impossible "+e.toString());
                }

                //Integer flgtr = info.getInt("flgTR");
                //String code = info.getString("code");
/*
                ArrayList<ItemShop> liste;
                liste = new ArrayList<>();

                // TODO : remplir la liste avec la réponse
                for (int i = 1; i <= 3; i++)
                    liste.add(new ItemShop("Magasin "+i , i, 0));

                controle.setListeMagasins(liste);

 */
            }
            else {
                if (message[0].equals("Erreur !")) {
                    Log.d("Erreur !", "******************* " + message[1]);
                }
            }
        } else {
            Log.d("Erreur !", " réponse invalide ******************* " );
        }
    }

    public void envoi(String operation, JSONArray lesDonneesJSON) {
        AccesHTTP accesDonnees = new AccesHTTP();
        // lien de délégation
        accesDonnees.delegate = this;
        // ajout parametres
        accesDonnees.addParam("operation", operation);
        if (lesDonneesJSON!=null)
            accesDonnees.addParam("lesdonnees", lesDonneesJSON.toString());
        // appel au serveur
        accesDonnees.execute(SERVERADDR);
    }

}
