package com.example.projetsae;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        Button normale = findViewById(R.id.normale);
        normale.setOnClickListener(v -> {
            // Lire le mode hard depuis les préférences et le passer à MainActivity
            SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
            boolean modeHard = prefs.getBoolean("modeHard", false);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("modeHard", modeHard);
            startActivity(intent);
        });

        Button destine = findViewById(R.id.destine);
        destine.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListeActivity.class);
            startActivity(intent);
        });

        Button regles = findViewById(R.id.regles);
        regles.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReglesActivity.class);
            startActivity(intent);
        });

        // Ouvre l'activité de configuration (mode hard), accessible seulement depuis le menu
        Button parametres = findViewById(R.id.parametres);
        parametres.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);
        });
    }

    // retour arrière → ferme l'application
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
