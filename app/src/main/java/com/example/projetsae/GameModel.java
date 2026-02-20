package com.example.projetsae;

import java.util.Random;

public class GameModel {



        private int[][] grille = new int[6][6];
        private Random rand = new Random();

        public GameModel() {
            genererGrille();
        }

        public int getCase(int i, int j) {
            return grille[i][j];
        }

        // Génération de la grille
        public void genererGrille() {

            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {

                    boolean recommence = true;

                    while (recommence) {

                        int couleur = rand.nextInt(7);
                        int countLigne = 0;
                        int countColonne = 0;

                        // Ligne
                        for (int k = 0; k < j; k++) {
                            if (grille[i][k] == couleur) {
                                countLigne++;
                            }
                        }

                        // Colonne
                        for (int k = 0; k < i; k++) {
                            if (grille[k][j] == couleur) {
                                countColonne++;
                            }
                        }

                        if (countLigne < 2 && countColonne < 2) {
                            grille[i][j] = couleur;
                            recommence = false;
                        }
                    }
                }
            }
        }

        // Décalage circulaire ligne droite
        public void decalerLigneDroite(int ligne) {
            int temp = grille[ligne][5];

            for (int j = 5; j > 0; j--) {
                grille[ligne][j] = grille[ligne][j - 1];
            }

            grille[ligne][0] = temp;
        }

        // Décalage circulaire colonne bas
        public void decalerColonneBas(int colonne) {
            int temp = grille[5][colonne];

            for (int i = 5; i > 0; i--) {
                grille[i][colonne] = grille[i - 1][colonne];
            }

            grille[0][colonne] = temp;
        }
    }

