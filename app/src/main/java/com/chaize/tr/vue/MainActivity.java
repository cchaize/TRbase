package com.chaize.tr.vue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private ImageButton btnShop;
    private ImageButton btnMaj;
    private ImageButton btnSetup;
    private ImageButton btnCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btnShop = findViewById(R.id.btnMagasin);
        btnShop.setOnClickListener(this);
        btnMaj = findViewById(R.id.btnMaj);
        btnMaj.setOnClickListener(this);
        btnSetup = findViewById(R.id.btnSetup);
        btnSetup.setOnClickListener(this);
        btnCourses = findViewById(R.id.btnCourses);
        btnCourses.setOnClickListener(this);
        if (Controle.getInstance(this).getMagasin().getSequence() == 0) {
            Toast.makeText(this, "Veuillez d'abord sélectionner un magasin", Toast.LENGTH_LONG);
            btnMaj.setEnabled(false);
            btnCourses.setEnabled(false);
        }
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnMagasin:
                intent = new Intent(MainActivity.this, ShopSelectionActivity.class);
                startActivityForResult(intent, Controle.SEL_MAGASIN);
                break;
            case R.id.btnCourses:
                intent = new Intent(MainActivity.this, PanierActivity.class);
                startActivity(intent);
                break;
            case R.id.btnMaj:
                intent = new Intent(MainActivity.this, MajActivity.class);
                startActivity(intent);
                break;
            case R.id.btnSetup:
                intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        switch (requestCode) {
            case Controle.SEL_MAGASIN:
                if (resultCode == Controle.SEL_MAGASIN_OK) {
                    btnMaj.setEnabled(true);
                    btnCourses.setEnabled(true);
                }
                break;
        }
    }

}
