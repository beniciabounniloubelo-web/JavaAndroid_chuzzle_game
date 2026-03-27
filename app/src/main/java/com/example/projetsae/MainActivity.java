package com.example.projetsae;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private LinearLayout overlay;
    private boolean enDrag = false;
    private int CELL_SIZE;
    private float debutX, debutY;
    private boolean modeHorizontal;

    private boolean modelDestine = false;
    private final int SEUIL = 20;
    private int ligneSelectionnee;
    private int colonneSelectionnee;

    private int le_score = 0;

    private long la_graine;
    private TextView score;
    private boolean directionFixee = false;
    private int nbCoups = 0;
    private TextView coups;

    private int[] images = {
            R.drawable.c1,
            R.drawable.c2,
            R.drawable.c3,
            R.drawable.c4,
            R.drawable.c5,
            R.drawable.c6,
            R.drawable.c7,
            R.drawable.cpersistante,
            R.drawable.cmystere
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = findViewById(R.id.table);
        overlay = findViewById(R.id.overlay);
        score = findViewById(R.id.score);
        coups = findViewById(R.id.coups);

        String seedExtra = getIntent().getStringExtra("seed");
        if (seedExtra != null) {
            modelDestine = true;
            la_graine = Long.parseLong(seedExtra);
        } else {
            modelDestine = false;
        }

        afficherDialogLancement();
    }

    private void afficherDialogLancement() {
        CheckBox checkHard = new CheckBox(this);
        checkHard.setText("Mode Hard");
        checkHard.setPadding(40, 20, 40, 20);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nouvelle partie")
                .setView(checkHard)
                .setCancelable(false)
                .setPositiveButton("Jouer", (dialog, which) -> {
                    boolean modeHard = checkHard.isChecked();
                    if (modelDestine) {
                        model = new GameModel(la_graine, modeHard);
                    } else {
                        model = new GameModel(modeHard);
                        la_graine = model.getGraine();
                    }
                    afficherGrille();
                    table.post(() -> {
                        TableRow row = (TableRow) table.getChildAt(0);
                        View v = row.getChildAt(0);
                        CELL_SIZE = v.getWidth();
                    });
                })
                .show();
    }

    private void afficherGrille() {
        table.removeAllViews();

        for (int i = 0; i < 6; i++) {

            TableRow row = new TableRow(this);

            for (int j = 0; j < 6; j++) {

                ImageView img = new ImageView(this);
                int val = model.getCase(i, j);
                if (val == -1) img.setForeground(ContextCompat.getDrawable(this, R.drawable.cmystere)); //pour les case mysteres
                else if (val == -2) {
                    img.setImageResource(images[model.getCouleurCachee(i, j)]); // couleur en fond
                    img.setForeground(ContextCompat.getDrawable(this, R.drawable.cpersistante)); //pour es cases persistantes
                }
                else {
                    img.setImageResource(images[val]);
                    img.setForeground(null); // important : effacer le foreground des cases normales
                }
                //img.setForeground(ContextCompat.getDrawable(this, R.drawable.cverrou)); //pour mettre les verrous
                img.setLayoutParams(new TableRow.LayoutParams(100, 100));
                final int ligne = i;
                final int colonne = j;

                img.setOnTouchListener((v, event) -> {


                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:

                                debutX = event.getRawX();
                                debutY = event.getRawY();

                                ligneSelectionnee = ligne;
                                colonneSelectionnee = colonne;

                                enDrag = true;
                                return true;

                            case MotionEvent.ACTION_MOVE:

                                if (!enDrag) return false;

                                float diffX = event.getRawX() - debutX;
                                float diffY = event.getRawY() - debutY;

                                if (overlay.getVisibility() == View.GONE) {

                                    if (Math.abs(diffX) > SEUIL || Math.abs(diffY) > SEUIL) {

                                        if (Math.abs(diffX) > Math.abs(diffY)) {
                                            modeHorizontal = true;
                                        } else {
                                            modeHorizontal = false;
                                        }
                                        directionFixee = true;
                                        creerOverlay();
                                        }


                                }
                                if (directionFixee) {
                                    if (modeHorizontal) {
                                        overlay.setTranslationX(diffX);
                                    } else {
                                        overlay.setTranslationY(diffY);
                                    }
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                                if (!enDrag) return false;

                                float finalX = event.getRawX() - debutX;
                                float finalY = event.getRawY() - debutY;

                                boolean mouvementFait = false;

                                int[][] sauvegarde = model.sauvegarderGrille(); //histoire de se rappeler de la gille avant un mouv

                                if (modeHorizontal) {
                                    int deplacement = Math.round(finalX / CELL_SIZE);
                                    while (deplacement > 0) {
                                        model.decalerLigneDroite(ligneSelectionnee);
                                        deplacement--;
                                        mouvementFait = true;
                                    }
                                    while (deplacement < 0) {
                                        model.decalerLigneGauche(ligneSelectionnee);
                                        deplacement++;
                                        mouvementFait = true;
                                    }
                                } else {
                                    int deplacement = Math.round(finalY / CELL_SIZE);
                                    while (deplacement > 0) {
                                        model.decalerColonneBas(colonneSelectionnee);
                                        deplacement--;
                                        mouvementFait = true;
                                    }
                                    while (deplacement < 0) {
                                        model.decalerColonneHaut(colonneSelectionnee);
                                        deplacement++;
                                        mouvementFait = true;
                                    }
                                }

                                // vérifier si le déplacement crée un alignement
                                if (model.aUnAlignement(model.getGrille())) {
                                    mouvementFait = true;
                                } else {
                                    model.restaurerGrille(sauvegarde); // aucun alignement → on annule
                                }

                                overlay.setVisibility(View.GONE);
                                overlay.removeAllViews();
                                overlay.setTranslationX(0);
                                overlay.setTranslationY(0);
                                directionFixee = false;
                                modeHorizontal = false;

                                enDrag = false;
                                afficherGrille();

                                if(mouvementFait) {
                                    nbCoups ++;
                                    coups.setText("nombre de coups : " + nbCoups);
                                }

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    boolean continuer = true;
                                    while (continuer) {
                                        int avant = le_score;
                                        verifierAlignements();
                                        continuer = (le_score > avant); // encore des combos ?
                                    }
                                    afficherGrille();

                                    // Fin de partie
                                    if (model.aucunCoupPossible()) { //verification si partie finie
                                        SharedPreferences prefs = getSharedPreferences("saves", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        int nb = prefs.getInt("nbParties", 0);
                                        editor.putLong("seed_" + nb, la_graine);
                                        editor.putInt("nbParties", nb + 1);
                                        editor.apply();
                                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                                .setTitle("Partie terminée !")
                                                .setMessage("Aucun coup possible.\nScore final : " + le_score+"\nGraine de la partie : "+ la_graine)
                                                .setPositiveButton("Rejouer", (dialog, which) -> { //soit on rejoue et tt est remis a 0
                                                    if (modelDestine) {
                                                        model = new GameModel(la_graine); // même graine
                                                    } else {
                                                        model = new GameModel();          // nouvelle graine aléatoire
                                                        la_graine = model.getGraine();
                                                    }
                                                    le_score = 0;
                                                    nbCoups = 0;
                                                    score.setText("Score : 0");
                                                    coups.setText("nombre de coups : 0");
                                                    afficherGrille();
                                                })
                                                .setNegativeButton("Menu", (dialog, which) -> finish()) //soit direction menu
                                                .setCancelable(false) //pas possible de fermer le dialog donc grille visible mais inutilisable
                                                .show(); //dialog rendu visible
                                    }
                                }, 2000);



                                return true;
                        }


                        return false;

                });
                row.addView(img);
            }

            table.addView(row);
        }
    }

    private int couleurEffective(int i, int j) { //histoire de renvoyer la vraie couleur de la case
        int val = model.getCase(i, j);
        if (val == -1 || val == -2) return model.getCouleurCachee(i, j);
        return val;
    }

    private int verifierAlignements() {
        int scoreGagne = 0;
        boolean aEuSuppresion; //pour aider a la reaction en cascade
        int combo = 0;

        do {
            boolean[][] aSupprimer = new boolean[6][6];
            boolean[][] aResister = new boolean[6][6];
            aEuSuppresion = false;

        // 1) détecter alignements horizontaux
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                if (couleurEffective(i,j) == couleurEffective(i,j-1)) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int k = 0; k < count; k++) {
                            int li = i, lj = j-1-k;
                            if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 1) { //si resistance, la diminue de 1
                                aResister[li][lj] = true;
                            } else if (model.getCase(li,lj) == -1) {
                                model.setCase(li, lj, model.getCouleurCachee(li, lj)); // si mystere, revele la couleur
                            } else {
                                aSupprimer[li][lj] = true; // sinon suppression normale
                            }
                        }
                        ajouterScore(count, combo);
                        aEuSuppresion = true;
                    }
                    count = 1;
                }
            }
            if (count >= 3) {
                for (int k = 0; k < count; k++) {
                    int li = i, lj = 6-1-k;
                    if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 1) {
                        aResister[li][lj] = true;
                    } else if (model.getCase(li,lj) == -1) {
                        model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                    } else {
                        aSupprimer[li][lj] = true; // suppression normale
                    }
                }
                ajouterScore(count, combo);
                aEuSuppresion = true;
            }
        }

        // 2) détecter alignements verticaux
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                if (couleurEffective(i,j) == couleurEffective(i-1,j)) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int k = 0; k < count; k++) {
                            int li = i-1-k, lj = j;
                            if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 1) {
                                aResister[li][lj] = true;
                            } else if (model.getCase(li,lj) == -1) {
                                model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                            } else {
                                aSupprimer[li][lj] = true; // suppression normale
                            }
                        }
                        ajouterScore(count, combo);
                        aEuSuppresion = true;
                    }
                    count = 1;
                }
            }
            if (count >= 3) {
                for (int k = 0; k < count; k++) {
                    int li = 6-1-k, lj = j;
                    if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 1) {
                        aResister[li][lj] = true;
                    } else if (model.getCase(li,lj) == -1) {
                        model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                    } else {
                        aSupprimer[li][lj] = true; // suppression normale
                    }
                }
                ajouterScore(count, combo);
                aEuSuppresion = true;
            }
        }

        // 3) appliquer le s resistances une seule fois
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (aResister[i][j]) {
                        model.setResistance(i, j, model.getResistance(i, j) - 1);
                        if (model.getResistance(i, j) == 0) {
                            aSupprimer[i][j] = true; // résistance épuisée, on supprime
                        }
                        aEuSuppresion = true;
                    }
                }
            }
        // 4) faire tomber les cases et remplir les vides
            if (aEuSuppresion) {
                for (int j = 0; j < 6; j++) {
                    int vide = 0;
                    for (int i = 5; i >= 0; i--) {
                        if (aSupprimer[i][j]) {
                            vide++;
                        } else if (vide > 0) {
                            model.setCase(i + vide, j, model.getCase(i, j));
                        }
                    }
                    // nouvelles cases en haut
                    for (int i = 0; i < vide; i++) {
                        model.setCase(i, j, model.prochaineCouleur(i, j)); //pour que la graine reste la meme
                        model.setCouleurCachee(i + vide, j, model.getCouleurCachee(i, j)); // comme ça quand une Persistante glisse vers le bas elle garde sa couleur cachée et sa résistance
                        model.setResistance(i + vide, j, model.getResistance(i, j));
                    }
                }
                combo++;
            }
        } while (aEuSuppresion);

        // ajouterScore(scoreGagne);
        return scoreGagne;
    }





    private void ajouterScore(int nb, int combo) {
        int points;
        if (nb == 3) points = 8;
        else if (nb == 4) points = 16;
        else if (nb == 5) points = 32;
        else points = 64;

        if (combo > 1){
            double bonus = 1 + 0.5 * (combo - 1);
            points = (int) Math.round(points*bonus);
        }

        le_score += points;
        score.setText("Score : " + le_score);
    }

    private void creerOverlay() {
        overlay.removeAllViews();

        int tableX = table.getLeft();
        int tableY = table.getTop();

        if (modeHorizontal) {
            overlay.setOrientation(LinearLayout.HORIZONTAL);

            TableRow row = (TableRow) table.getChildAt(ligneSelectionnee);

            overlay.setX(tableX);
            overlay.setY(tableY + row.getTop());

            for (int j = 0; j < row.getChildCount(); j++) {
                ImageView original = (ImageView) row.getChildAt(j);
                ImageView copy = new ImageView(this);
                copy.setImageDrawable(original.getDrawable());
                copy.setLayoutParams(original.getLayoutParams());
                overlay.addView(copy);
                original.setVisibility(View.INVISIBLE);
            }

        } else {
            overlay.setOrientation(LinearLayout.VERTICAL);

            TableRow firstRow = (TableRow) table.getChildAt(0);
            View firstCell = firstRow.getChildAt(colonneSelectionnee);

            overlay.setX(tableX + firstCell.getLeft());
            overlay.setY(tableY);

            int currentY = 0;
            for (int i = 0; i < table.getChildCount(); i++) {
                TableRow row = (TableRow) table.getChildAt(i);
                ImageView original = (ImageView) row.getChildAt(colonneSelectionnee);
                ImageView copy = new ImageView(this);
                copy.setImageDrawable(original.getDrawable());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    original.getWidth(),
                    original.getHeight()
                );
                copy.setLayoutParams(params);
                overlay.addView(copy);
                original.setVisibility(View.INVISIBLE);
            }
            overlay.setY(table.getTop());
        }

        overlay.setVisibility(View.VISIBLE);
    }
    /*private void creerOverlay() {

        overlay.removeAllViews();

        if (modeHorizontal) {

            overlay.setOrientation(LinearLayout.HORIZONTAL);

            TableRow row = (TableRow) table.getChildAt(ligneSelectionnee);

            View firstCell = row.getChildAt(0);

            int[] loc = new int[2];
            firstCell.getLocationOnScreen(loc);

            int[] tablePos = new int[2];
            table.getLocationOnScreen(tablePos);

            float posX = tablePos[0] + loc[0];
            float posY = loc[1];

            overlay.setX(posX);
            overlay.setY(posY);

            for (int j = 0; j < row.getChildCount(); j++) {

                ImageView original = (ImageView) row.getChildAt(j);

                ImageView copy = new ImageView(this);
                copy.setImageDrawable(original.getDrawable());
                copy.setLayoutParams(original.getLayoutParams());

                overlay.addView(copy);

                original.setVisibility(View.INVISIBLE);
            }

        } else {



            overlay.setOrientation(LinearLayout.VERTICAL);
            // récupérer la position X de la colonne sélectionnée
            TableRow firstRow = (TableRow) table.getChildAt(0);
            View firstCell = firstRow.getChildAt(colonneSelectionnee);

            int[] loc = new int[2];
            firstCell.getLocationOnScreen(loc);
            int[] tablePos = new int[2];
            table.getLocationOnScreen(tablePos);
            float posX = loc[0];
            float posY = tablePos[1] - loc[1];



            overlay.setX(posX); // positionner l'overlay sur la bonne colonne
            overlay.setY(posY);

            for (int i = 0; i < table.getChildCount(); i++) {

                TableRow row = (TableRow) table.getChildAt(i);
                ImageView original = (ImageView) row.getChildAt(colonneSelectionnee);

                ImageView copy = new ImageView(this);
                copy.setImageDrawable(original.getDrawable());
                copy.setLayoutParams(original.getLayoutParams());

                overlay.addView(copy);

                original.setVisibility(View.INVISIBLE);
            }
        }

        overlay.setVisibility(View.VISIBLE);


    }*/

/* A mettre à la fin d'une partie


SharedPreferences prefs = getSharedPreferences("saves", MODE_PRIVATE);
SharedPreferences.Editor editor = prefs.edit();

// récupérer nombre de parties déjà sauvegardées
int nb = prefs.getInt("nbParties", 0);

// sauvegarder la seed
editor.putLong("seed_" + nb, seed);

// incrémenter compteur
editor.putInt("nbParties", nb + 1);

editor.apply();
*/
}