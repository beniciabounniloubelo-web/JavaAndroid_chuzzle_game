package com.example.projetsae;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ReglesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regles);

        // Bouton retour → menu
        Button btnRetour = findViewById(R.id.btn_retour);
        btnRetour.setOnClickListener(v -> finish());
    }

    // retour arrière → menu
    @Override
    public void onBackPressed() {
        finish();
    }
}
