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


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private LinearLayout overlay;
    private boolean enDrag = false;
    private int CELL_SIZE;
    private float debutX, debutY;
    private boolean modeHorizontal;

    private final int SEUIL = 20;
    private int ligneSelectionnee;
    private int colonneSelectionnee;

    private int le_score = 0;
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
            R.drawable.c7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new GameModel();
        table = findViewById(R.id.table);
        overlay = findViewById(R.id.overlay);
        score = findViewById(R.id.score);
        coups = findViewById(R.id.coups);

        afficherGrille();
        table.post(() -> {
            TableRow row = (TableRow) table.getChildAt(0);
            View v = row.getChildAt(0);
            CELL_SIZE = v.getWidth();
        });


    }

    private void afficherGrille() {
        table.removeAllViews();

        for (int i = 0; i < 6; i++) {

            TableRow row = new TableRow(this);

            for (int j = 0; j < 6; j++) {

                ImageView img = new ImageView(this);
                img.setImageResource(images[model.getCase(i, j)]);
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
                                    int scoreGagne = verifierAlignements();
                                    afficherGrille();
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

    private int verifierAlignements() {
        int scoreGagne = 0;
        boolean[][] aSupprimer = new boolean[6][6];

        // 1) détecter alignements horizontaux
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                if (model.getCase(i,j) == model.getCase(i,j-1)) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int k = 0; k < count; k++) aSupprimer[i][j-1-k] = true;
                        ajouterScore(count);
                    }
                    count = 1;
                }
            }
            if (count >= 3) {
                for (int k = 0; k < count; k++) aSupprimer[i][6-1-k] = true;
                ajouterScore(count);
            }
        }

        // 2) détecter alignements verticaux
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                if (model.getCase(i,j) == model.getCase(i-1,j)) {
                    count++;
                } else {
                    if (count >= 3) {
                        for (int k = 0; k < count; k++) aSupprimer[i-1-k][j] = true;
                        ajouterScore(count);
                    }
                    count = 1;
                }
            }
            if (count >= 3) {
                for (int k = 0; k < count; k++) aSupprimer[6-1-k][j] = true;
                ajouterScore(count);
            }
        }

        // 3) faire tomber les cases et remplir les vides
        Random rand = new Random();
        for (int j = 0; j < 6; j++) {
            int vide = 0;
            for (int i = 5; i >= 0; i--) {
                if (aSupprimer[i][j]) {
                    vide++;
                } else if (vide > 0) {
                    model.setCase(i+vide, j, model.getCase(i,j));
                }
            }
            // nouvelles cases en haut
            for (int i = 0; i < vide; i++) {
                model.setCase(i, j, rand.nextInt(7));
            }
        }

        // ajouterScore(scoreGagne);
        return scoreGagne;
    }





    private void ajouterScore(int nb) {

        if (nb == 3) le_score += 8;
        if (nb == 4) le_score += 16;
        if (nb == 5) le_score += 32;
        if (nb >= 6) le_score += 64;

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