package com.example.projetsae;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ListeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_destine);

        LinearLayout listeLayout = findViewById(R.id.liste_layout);

        SharedPreferences prefs = getSharedPreferences("saves", MODE_PRIVATE);
        int nb = prefs.getInt("nbParties", 0);

        for (int i = 0; i < nb; i++) {
            final int index = i;
            Button btn = new Button(this);
            btn.setText("Partie " + (i + 1));
            btn.setOnClickListener(v -> {
                long seed = prefs.getLong("seed_" + index, 0);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("seed", String.valueOf(seed));
                startActivity(intent);
            });
            listeLayout.addView(btn);
        }

        Button btnJouer = findViewById(R.id.btn_jouer_graine);
        EditText champGraine = findViewById(R.id.champ_graine);

        btnJouer.setOnClickListener(v -> {
            String texte = champGraine.getText().toString().trim();
            if (texte.isEmpty()) {
                Toast.makeText(this, "Entre une graine !", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                long seed = Long.parseLong(texte);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("seed", String.valueOf(seed));
                startActivity(intent);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "La graine doit être un nombre !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // retour arrière → menu
    @Override
    public void onBackPressed() {
        finish();
    }
}
