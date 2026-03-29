package com.example.projetsae;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameController controller;

    private TableLayout table;
    private boolean enDrag = false;
    private int CELL_SIZE;
    private float debutX, debutY;
    private boolean modeHorizontal;

    private final int SEUIL = 50;
    private int ligneSelectionnee;
    private int colonneSelectionnee;
    private boolean directionFixee = false;

    private TextView score;
    private TextView coups;

    private LinearLayout bandeauFin;
    private TextView bandeauScore;
    private TextView bandeauGraine;
    private Button bandeauBtnRejouer;
    private Button bandeauBtnMenu;

    private int[] images = {
            R.drawable.c1, R.drawable.c2, R.drawable.c3,
            R.drawable.c4, R.drawable.c5, R.drawable.c6,
            R.drawable.c7, R.drawable.cpersistante, R.drawable.cmystere
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table             = findViewById(R.id.table);
        score             = findViewById(R.id.score);
        coups             = findViewById(R.id.coups);
        bandeauFin        = findViewById(R.id.bandeau_fin);
        bandeauScore      = findViewById(R.id.bandeau_score);
        bandeauGraine     = findViewById(R.id.bandeau_graine);
        bandeauBtnRejouer = findViewById(R.id.bandeau_btn_rejouer);
        bandeauBtnMenu    = findViewById(R.id.bandeau_btn_menu);

        if (savedInstanceState != null) {
            long graine      = savedInstanceState.getLong("graine");
            boolean modeHard = savedInstanceState.getBoolean("modeHard");
            boolean destine  = savedInstanceState.getBoolean("destine");

            GameModel model       = new GameModel(graine, modeHard);
            int[][] grille        = new int[6][6];
            int[][] couleurCachee = new int[6][6];
            int[][] resistance    = new int[6][6];
            boolean[][] verrous   = new boolean[6][6];

            for (int i = 0; i < 6; i++) {
                grille[i]        = savedInstanceState.getIntArray("grille_" + i);
                couleurCachee[i] = savedInstanceState.getIntArray("couleurCachee_" + i);
                resistance[i]    = savedInstanceState.getIntArray("resistance_" + i);
                int[] verrouInt  = savedInstanceState.getIntArray("verrou_" + i);
                for (int j = 0; j < 6; j++) verrous[i][j] = verrouInt[j] == 1;
            }

            model.restaurerGrille(grille);
            model.restaurerCouleurCachee(couleurCachee);
            model.restaurerResistance(resistance);
            model.restaurerVerrous(verrous);

            controller = new GameController(model, graine, destine);
            controller.setScore(savedInstanceState.getInt("score"));
            controller.setNbCoups(savedInstanceState.getInt("nbCoups"));

            mettreAJourAffichage();
            afficherGrille();

        } else {
            boolean modeHard = getIntent().getBooleanExtra("modeHard", false);
            String seedExtra = getIntent().getStringExtra("seed");

            GameModel model;
            long graine;
            boolean destine;

            if (seedExtra != null) {
                destine = true;
                graine  = Long.parseLong(seedExtra);
                model   = new GameModel(graine, modeHard);
            } else {
                destine = false;
                model   = new GameModel(modeHard);
                graine  = model.getGraine();
            }

            controller = new GameController(model, graine, destine);
            afficherGrille();
            table.post(() -> {
                TableRow row = (TableRow) table.getChildAt(0);
                View v = row.getChildAt(0);
                CELL_SIZE = v.getWidth();
            });
        }
    }

    @Override
    public void onBackPressed() { finish(); }

    private void mettreAJourAffichage() {
        score.setText("Score : " + controller.getScore());
        coups.setText("nombre de coups : " + controller.getNbCoups());
    }

    private void afficherBandeauFin() {
        bandeauScore.setText("Score final : " + controller.getScore());
        bandeauGraine.setText("Graine : " + controller.getGraine());
        bandeauBtnRejouer.setOnClickListener(v -> {
            bandeauFin.setVisibility(View.GONE);
            controller.rejouer();
            mettreAJourAffichage();
            afficherGrille();
        });
        bandeauBtnMenu.setOnClickListener(v -> finish());
        bandeauFin.setVisibility(View.VISIBLE);
    }

    private void resoudreEtVerifierFin() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean continuer = true;
            while (continuer) {
                int avant = controller.getScore();
                controller.verifierAlignements();
                continuer = controller.getScore() > avant;
            }
            mettreAJourAffichage();
            afficherGrille();

            if (controller.getModel().aucunCoupPossible()) {
                controller.sauvegarderPartie(MainActivity.this);
                afficherBandeauFin();
            }
        }, 500);
    }

    private void afficherGrille() {
        table.removeAllViews();
        GameModel model = controller.getModel();

        for (int i = 0; i < 6; i++) {
            TableRow row = new TableRow(this);
            for (int j = 0; j < 6; j++) {
                ImageView img = new ImageView(this);
                int val = model.getCase(i, j);

                if (val == -1) {
                    img.setForeground(getDrawable(R.drawable.cmystere));
                } else if (val == -2) {
                    img.setImageResource(images[model.getCouleurCachee(i, j)]);
                    img.setForeground(getDrawable(R.drawable.cpersistante));
                } else if (val >= 0 && val <= 6) {
                    img.setImageResource(images[val]);
                    img.setForeground(null);
                } else {
                    img.setImageResource(0);
                    img.setForeground(null);
                }
                if (model.getVerrou(i, j)) {
                    img.setForeground(getDrawable(R.drawable.cverrou));
                }

                img.setLayoutParams(new TableRow.LayoutParams(100, 100));
                final int ligne   = i;
                final int colonne = j;

                img.setOnTouchListener((v, event) -> {
                    if (bandeauFin.getVisibility() == View.VISIBLE) return true;

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            debutX              = event.getRawX();
                            debutY              = event.getRawY();
                            ligneSelectionnee   = ligne;
                            colonneSelectionnee = colonne;
                            enDrag              = true;
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            if (!enDrag) return false;
                            float diffX = event.getRawX() - debutX;
                            float diffY = event.getRawY() - debutY;
                            if (!directionFixee) {
                                if (Math.abs(diffX) > SEUIL || Math.abs(diffY) > SEUIL) {
                                    modeHorizontal = Math.abs(diffX) > Math.abs(diffY);
                                    directionFixee = true;
                                }
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!enDrag) return false;

                            float finalX = event.getRawX() - debutX;
                            float finalY = event.getRawY() - debutY;

                            // Calculer le déplacement selon la direction
                            int ligneOuColonne = modeHorizontal ? ligneSelectionnee : colonneSelectionnee;
                            int deplacement    = modeHorizontal
                                    ? Math.round(finalX / CELL_SIZE)
                                    : Math.round(finalY / CELL_SIZE);

                            directionFixee = false;
                            modeHorizontal = false;
                            enDrag         = false;

                            // Déléguer entièrement au Controller
                            boolean coupAccepte = controller.tenterDeplacement(
                                    ligneSelectionnee == ligneOuColonne, // horizontal si ligneOuColonne = ligne
                                    ligneOuColonne,
                                    deplacement
                            );

                            afficherGrille();

                            if (coupAccepte) {
                                mettreAJourAffichage();
                                resoudreEtVerifierFin();
                            }

                            return true;
                    }
                    return false;
                });
                row.addView(img);
            }
            table.addView(row);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        GameModel model = controller.getModel();

        int[][] grille = model.getGrille();
        for (int i = 0; i < 6; i++) outState.putIntArray("grille_" + i, grille[i]);

        for (int i = 0; i < 6; i++) {
            outState.putIntArray("couleurCachee_" + i, model.getCouleurCacheeLigne(i));
            outState.putIntArray("resistance_" + i,    model.getResistanceLigne(i));
        }

        for (int i = 0; i < 6; i++) {
            boolean[] verrouLigne = model.getVerrouLigne(i);
            int[] verrouInt = new int[6];
            for (int j = 0; j < 6; j++) verrouInt[j] = verrouLigne[j] ? 1 : 0;
            outState.putIntArray("verrou_" + i, verrouInt);
        }

        outState.putInt("score",        controller.getScore());
        outState.putInt("nbCoups",      controller.getNbCoups());
        outState.putLong("graine",      controller.getGraine());
        outState.putBoolean("modeHard", model.isModeHard());
        outState.putBoolean("destine",  controller.isModelDestine());
    }
}