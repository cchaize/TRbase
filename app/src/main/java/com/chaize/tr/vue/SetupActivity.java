package com.chaize.tr.vue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.outils.InternetCheck;


public class SetupActivity extends AppCompatActivity implements OnClickListener {

    private Button btnResetLocal;
    private Button btnTestDistant;
    private Button btnSynchroniser;
    private Button btnListeProduits;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        init();
    }

    private void init() {
        btnResetLocal = findViewById(R.id.btnResetLocal);
        btnResetLocal.setOnClickListener(this);
        btnTestDistant = findViewById(R.id.btnTestDistant);
        btnTestDistant.setOnClickListener(this);
        btnSynchroniser = findViewById(R.id.btnSynchroniser);
        btnSynchroniser.setOnClickListener(this);
        btnListeProduits= findViewById(R.id.btnListeProduits);
        btnListeProduits.setOnClickListener(this);
        txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setMovementMethod(new ScrollingMovementMethod());
        refreshStatus();
    }

    public void onClick(View v){
        Intent intent;
        switch (v.getId()) {
            case R.id.btnResetLocal:
                DbHelper dbHelper = new DbHelper(this);
                dbHelper.resetDatabase();
                dbHelper.close();

                Toast.makeText(this, "Reset base locale", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnTestDistant:
                btnTestDistant.setText("Connecting....");
                new InternetCheck(internet -> {
                    if (internet) {
                        btnTestDistant.setText("Connecté");
                    } else {
                        btnTestDistant.setText("Non connecté");
                    }
                });
                break;
            case R.id.btnSynchroniser:
                DbHelper.synchronize(SetupActivity.this, 1);
                break;
            case R.id.btnListeProduits:
                intent = new Intent(SetupActivity.this, ListeProduitsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        refreshStatus();
    }

    public void refreshStatus(){
        btnTestDistant.setText("Connecting....");
        new InternetCheck(internet -> {
            if (internet) {
                btnTestDistant.setText("Connecté");
            } else {
                btnTestDistant.setText("Non connecté");
            }
        });
        String res;
        DbHelper dbHelper = new DbHelper(this);
        txtStatus.setText(dbHelper.status());
        txtStatus.append("\n-----------------------------------\n");
        txtStatus.append(Controle.getInstance(this).getLog());
        dbHelper.close();
    }
}
