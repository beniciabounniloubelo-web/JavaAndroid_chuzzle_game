package com.example.projetsae;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private LinearLayout overlay;
    private boolean enDrag = false;
    private int CELL_SIZE;
    private int SEUIL =  20;
    private float debutX, debutY;

    private float dernierX = 0;
    private float dernierY = 0;

    private boolean directionChoisie = false;
    private boolean modeHorizontal;

    private int ligneSelectionnee;
    private int colonneSelectionnee;

    private int le_score = 0;
    private TextView score;

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
                img.setLayoutParams(new TableRow.LayoutParams(150, 150));
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
                                    creerOverlay();
                                }

                                if (modeHorizontal) {
                                    overlay.setTranslationX(diffX);
                                } else {
                                    overlay.setTranslationY(diffY);
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                                if (!enDrag) return false;

                                float finalX = event.getRawX() - debutX;
                                float finalY = event.getRawY() - debutY;

                                if (modeHorizontal) {

                                    int deplacement = Math.round(finalX / CELL_SIZE);

                                    while (deplacement > 0) {
                                        model.decalerLigneDroite(ligneSelectionnee);
                                        deplacement--;
                                    }

                                    while (deplacement < 0) {
                                        model.decalerLigneGauche(ligneSelectionnee);
                                        deplacement++;
                                    }
                                } else {

                                    int deplacement = Math.round(finalY / CELL_SIZE);

                                    while (deplacement > 0) {
                                        model.decalerColonneBas(colonneSelectionnee);
                                        deplacement--;
                                    }

                                    while (deplacement < 0) {
                                        model.decalerColonneHaut(colonneSelectionnee);
                                        deplacement++;
                                    }
                                }

                                overlay.setVisibility(View.GONE);
                                overlay.removeAllViews();
                                overlay.setTranslationX(0);
                                overlay.setTranslationY(0);

                                enDrag = false;
                                afficherGrille();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    verifierAlignements();
                                    afficherGrille();
                                }, 200);



                                return true;
                        }


                        return false;

                });
                row.addView(img);
            }

            table.addView(row);
        }
    }


    private void verifierAlignements() {

        boolean[][] aSupprimer = new boolean[6][6];

        // vérifier lignes
        for (int i = 0; i < 6; i++) {

            int compteur = 1;

            for (int j = 1; j < 6; j++) {

                if (model.getCase(i,j) == model.getCase(i,j-1)) {
                    compteur++;
                } else {

                    if (compteur >= 3) {

                        for (int k = 0; k < compteur; k++) {
                            aSupprimer[i][j-1-k] = true;
                        }

                        ajouterScore(compteur);
                    }

                    compteur = 1;
                }
            }

            if (compteur >= 3) {

                for (int k = 0; k < compteur; k++) {
                    aSupprimer[i][5-k] = true;
                }

                ajouterScore(compteur);
            }
        }


        // vérifier colonnes
        for (int j = 0; j < 6; j++) {

            int compteur = 1;

            for (int i = 1; i < 6; i++) {

                if (model.getCase(i,j) == model.getCase(i-1,j)) {
                    compteur++;
                } else {

                    if (compteur >= 3) {

                        for (int k = 0; k < compteur; k++) {
                            aSupprimer[i-1-k][j] = true;
                        }

                        ajouterScore(compteur);
                    }

                    compteur = 1;
                }
            }

            if (compteur >= 3) {

                for (int k = 0; k < compteur; k++) {
                    aSupprimer[5-k][j] = true;
                }

                ajouterScore(compteur);
            }
        }

        remplacerCases(aSupprimer);
    }

    private void ajouterScore(int nb) {

        if (nb == 3) le_score += 8;
        if (nb == 4) le_score += 16;
        if (nb == 5) le_score += 32;
        if (nb >= 6) le_score += 64;

        score.setText("Score : " + le_score);
    }

    private void remplacerCases(boolean[][] aSupprimer) {

        Random rand = new Random();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {

                if (aSupprimer[i][j]) {

                    int nouvelle = rand.nextInt(7);
                    model.setCase(i,j,nouvelle);
                }
            }
        }
    }
    private void creerOverlay() {

        overlay.removeAllViews();

        if (modeHorizontal) {

            overlay.setOrientation(LinearLayout.HORIZONTAL);

            TableRow row = (TableRow) table.getChildAt(ligneSelectionnee);

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


    }


    private void resetTranslations() {

        for (int i = 0; i < table.getChildCount(); i++) {

            TableRow row = (TableRow) table.getChildAt(i);

            row.setTranslationX(0);

            for (int j = 0; j < row.getChildCount(); j++) {
                View cell = row.getChildAt(j);
                cell.setTranslationY(0);
            }
        }
    }

}