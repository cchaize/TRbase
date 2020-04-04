package com.chaize.tr.vue;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.outils.ItemClickSupport;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class PanierActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    PanierRecyclerAdapter adapter;
    ArrayList<ItemPanier> arrayList = new ArrayList<>();

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            int pos = viewHolder.getAdapterPosition();
            if (swipeDir == ItemTouchHelper.LEFT) {
                deleteItem(pos);
            } else {
                ItemPanier itemSelected = arrayList.get(pos);
                updateProduitFromPanier(itemSelected);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }
    };

    public void deleteItem(int pos) {
        ItemPanier itemSelected = arrayList.get(pos);
        String code = itemSelected.getProduit().getCode();
        DbHelper dbHelper = new DbHelper(PanierActivity.this);
        dbHelper.removeFromPanier(code, dbHelper.getWritableDatabase());
        dbHelper.close();
        arrayList.remove(pos);
        readPanier();
        // this line animates what happens after delete
        adapter.notifyItemRemoved(pos);
        Snackbar snackbar = Snackbar.make(recyclerView, "delete successful", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        JSONObject jsonParam = new JSONObject();
                        try {
                            jsonParam.put("code", code);
                            jsonParam.put("quantite", itemSelected.getQuantite());
                            jsonParam.put("prix", itemSelected.getPrix());
                            jsonParam.put("position", itemSelected.getPosition());
                            saveToPanier(jsonParam);
                            readPanier();
                            Snackbar snackbar1 = Snackbar.make(recyclerView, "Produit remis dans le panier!", Snackbar.LENGTH_SHORT);
                            snackbar1.show();
                        } catch (JSONException e) {
                            Snackbar snackbar1 = Snackbar.make(recyclerView, "erreur!", Snackbar.LENGTH_SHORT);
                            snackbar1.show();
                        }

                    }
                });
        snackbar.show();
    }

    private void updateProduitFromPanier(ItemPanier itemSelected) {
        Intent intent;
        intent = new Intent(PanierActivity.this, MajActivity.class);
        intent.putExtra(Controle.EXTRA_PRODUIT, itemSelected.getProduit());
        startActivityForResult(intent, Controle.MAJ_PRODUIT_FROM_PANIER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);
        recyclerView = (RecyclerView) findViewById(R.id.panierRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new PanierRecyclerAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView, R.layout.row_panier_layout)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        demanderPrix(arrayList.get(position).getProduit().getCode(), arrayList.get(position).getPrix(), arrayList.get(position).getQuantite());
                    }
                });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        readPanier();
    }

    public void addNoScan(View view) {
        EditText txtEan = findViewById(R.id.txtEAN);
        demanderPrix(txtEan.getText().toString());
    }

    public void scanProduit(View view) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Controle.MAJ_PRODUIT_FROM_PANIER:
                // On relit le panier pour récupérer la désignation qui aurait pu être mise à jour
                readPanier();

        }
        if (intent != null) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case "com.google.zxing.client.android.SCAN":
                    IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                    if (scanningResult != null) {
                        String codeEAN = scanningResult.getContents();
                        EditText txtEan = findViewById(R.id.txtEAN);
                        txtEan.setText(codeEAN);
                        demanderPrix(codeEAN);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No scan data received!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
            }
        }
    }

    public void demanderPrix(String code) {
        demanderPrix(code, 0.0f, 0);
    }

    public void demanderPrix(String code, float prix, int quantite) {
        final boolean[] result = new boolean[1];
        EditPrix editPrix;
        NumberPicker editQuantite;

        AlertDialog.Builder builder = new AlertDialog.Builder(PanierActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = PanierActivity.this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dlg_prix_layout, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogLayout);
        try {
            editPrix = dialogLayout.findViewById(R.id.txtDemandePrix);
            editQuantite = dialogLayout.findViewById(R.id.txtDemandeQty);

            if (prix == 0.0f) {
                DbHelper dbHelper = new DbHelper(PanierActivity.this);
                int seqMagasin = Controle.getInstance(PanierActivity.this).getMagasin().getSequence();
                Produit produit = dbHelper.readProduitFromLocalDatabase(code, seqMagasin, dbHelper.getReadableDatabase());
                editPrix.setText(String.valueOf(produit.getPrix()));
                dbHelper.close();
            } else {
                editPrix.setText(String.valueOf(prix));
            }
            editQuantite.setMinValue(1);
            editQuantite.setMaxValue(10);
            editQuantite.setWrapSelectorWheel(true);
            editQuantite.setValue(quantite);
            // Add action buttons
            EditPrix finalEditPrix = editPrix;
            NumberPicker finalEditQty = editQuantite;
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        float prix = finalEditPrix.getPrix();
                        int qty = finalEditQty.getValue();
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("code", code);
                        jsonParam.put("quantite", qty);
                        jsonParam.put("prix", prix);
                        jsonParam.put("position", arrayList.size() + 1);

                        saveToPanier(jsonParam);
                    } catch (JSONException e) {
                        Controle.addLog(Controle.typeLog.ERROR, "PanierActivity.OnActivityResult " + e.getLocalizedMessage());
                    }
                }
            })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                        }
                    });

            builder.create().show();
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, e.getLocalizedMessage());
        }
    }


    public void resetPanier(View view) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(PanierActivity.this);
        builder.setMessage("Supprimer tous les éléments du panier ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DbHelper dbHelper = new DbHelper(PanierActivity.this);
                        dbHelper.resetPanier(dbHelper.getWritableDatabase());
                        readPanier();
                        dbHelper.close();
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

    public void saveToPanier(JSONObject jsonParam) {
        DbHelper dbHelper = new DbHelper(PanierActivity.this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveProduitToPanier(jsonParam, database);
        readPanier();
        dbHelper.close();
    }

    private void readPanier() {
        arrayList.clear();

        DbHelper dbHelper = new DbHelper(PanierActivity.this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readPanierFromLocalDatabase(database);
        float totalTR = 0;

        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String code = cursor.getString(cursor.getColumnIndex(DbContract.PAN_CODE));
                    int quantite = cursor.getInt(cursor.getColumnIndex(DbContract.PAN_QUANTITE));
                    float prix = cursor.getFloat(cursor.getColumnIndex(DbContract.PAN_PRIX));
                    int position = cursor.getInt(cursor.getColumnIndex(DbContract.PAN_POSITION));
                    Produit produit = dbHelper.readProduitFromLocalDatabase(code, Controle.getInstance(PanierActivity.this).getMagasin().getSequence(), database);
                    produit.setPrix(prix);
                    arrayList.add(new ItemPanier(produit, quantite, prix, position));
                    if (produit.getFlgTR() == 1)
                        totalTR += prix * quantite;
                }
                cursor.close();
            }
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, "PanierActivity.readPanier " + e.getLocalizedMessage());
        }

        TextView txtTotal = findViewById(R.id.txtTotalTR);
        txtTotal.setText(Float.toString(totalTR));
        adapter.notifyDataSetChanged();

        dbHelper.close();

    }

}
