package com.chaize.tr.vue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.controleur.TRexception;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.outils.JsonTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MajActivity extends AppCompatActivity implements OnClickListener {

    private Button scanBtn;
    private ImageButton btnRefresh;
    private ImageButton btnDescRefresh;
    private TextView codeTxt;
    private TextView descTxt;
    private EditPrix editPrix;
    private Controle controle;
    private RadioButton rdTR, rdNoTR;
    private Button btnConfirm, btnCancel;
    private boolean unique=false;   // indique si plusieurs produits peuvent être mis à jour (false) ou seulement un seul (true)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maj);
        init();


    }

    private void init() {
        this.controle = Controle.getInstance(this);

        scanBtn = findViewById(R.id.scan_button);
        codeTxt = findViewById(R.id.scan_content);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnDescRefresh = findViewById(R.id.btnDescRefresh);
        descTxt = findViewById(R.id.description);
        editPrix = findViewById(R.id.editPrix);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        rdTR = findViewById(R.id.rdTR);
        rdNoTR = findViewById(R.id.rdNoTR);

        scanBtn.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnDescRefresh.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        ((TextView)findViewById(R.id.textNomMagasin)).setText(this.controle.getMagasin().getNom());
        ((TextView)findViewById(R.id.textVille)).setText(this.controle.getMagasin().getVille());

        Intent intent = getIntent();
        Produit produit = (Produit)intent.getSerializableExtra(Controle.EXTRA_PRODUIT);
        if (produit!=null) {
            unique=true;    // on a reçu un produit, on ne permet de mettre à jour que celui-ci
            Controle.getInstance(this).setProduit(produit);
            afficheProduit();
        }
    }

    public void onClick(View v){
        switch (v.getId()) {
            case R.id.scan_button:
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.initiateScan();
                break;

            case R.id.btnDescRefresh:
                new JsonTask(MajActivity.this, descTxt).execute("https://world.openfoodfacts.org/api/v0/product/"+codeTxt.getText()+".json");
                break;
            case R.id.btnRefresh:
                if (codeTxt.getText()!="") {
                    try {
                        this.controle.chercheProduit(codeTxt.getText().toString());
                    } catch (TRexception tRexception) {
                        alerter(tRexception.getMessage());
                    }
                }
                break;

            case R.id.btnConfirm:
                Toast.makeText(MajActivity.this,"Demande confirmation",Toast.LENGTH_SHORT).show();
                //ConfirmModification confirm = new ConfirmModification();
                confirmer();
                break;

            case R.id.btnCancel:
                codeTxt.setText("");
                descTxt.setText("");
                rdTR.setChecked(false);
                rdNoTR.setChecked(false);
                break;

            default:
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            try {
                this.controle.chercheProduit(scanContent);
            } catch (TRexception tRexception) {
                alerter(tRexception.getMessage());
            }
            afficheProduit();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void afficheProduit(){
        //public void afficheProduit(String code){

        //this.controle.chercheProduit(code, this);

        Integer flgTR = this.controle.getFlgTR();
        String txtFlg;

        codeTxt.setText(this.controle.getCode());
        descTxt.setText(this.controle.getDescription());
        switch (flgTR){
            case 1:
                txtFlg = "Eligible";
                // Afficher TR éligible en couleur
                rdTR.setChecked(true);
                break;
            case 2:
                txtFlg = "Non éligible";
                // Afficher TR non éligible en couleur
                rdNoTR.setChecked(true);
                break;
            default:
                rdTR.setChecked(false);
                rdNoTR.setChecked(false);
                txtFlg = "inconnu";
        }
        editPrix.setText(this.controle.getPrix().toString());
        Toast toast = Toast.makeText(getApplicationContext(),
                txtFlg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void confirmer() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(MajActivity.this);
        builder.setMessage("Confirmez-vous la modification ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Enregistrer la modif
                        //controle.enregProduit(codeTxt.getText().toString(), descTxt.getText().toString(), rdTR.isChecked()?1:rdNoTR.isChecked()?2:0, MajActivity.this );
                        DbHelper dbHelper = new DbHelper(MajActivity.this);
                        try {
                        Produit produit = new Produit(codeTxt.getText().toString(),
                                Controle.getInstance(MajActivity.this).getMagasin().getSequence(),
                                descTxt.getText().toString(),
                                rdTR.isChecked() ? 1 : (rdNoTR.isChecked() ? 2 : 0),
                                editPrix.getPrix(),
                                DbContract.SYNC_STATUS_FAILED);
                            dbHelper.saveProduitToLocalDatabase(produit, produit.getSyncStatus(), dbHelper.getWritableDatabase());
                        } catch (Exception e) {
                            Controle.addLog(Controle.typeLog.ERROR, "MajActivity.confirmer.onClick "+e.getLocalizedMessage());
                            Toast.makeText(MajActivity.this, "L'enregistrement a échoué", Toast.LENGTH_LONG);
                        }
                        dbHelper.close();
                        if (unique)
                            MajActivity.this.finish();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // ne rien faire
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();

    }

    private void alerter(String mes) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(MajActivity.this);
        builder.setMessage(mes);
/*                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Enregistrer la modif
                        controle.enregProduit(codeTxt.getText().toString(), descTxt.getText().toString(), rdTR.isChecked()?1:rdNoTR.isChecked()?2:0, MajActivity.this );
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // ne rien faire
                    }
                });
*/
        // Create the AlertDialog object and return it
        builder.create().show();
    }
}
