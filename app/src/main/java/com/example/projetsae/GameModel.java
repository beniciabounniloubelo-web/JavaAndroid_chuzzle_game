package com.example.projetsae;

import java.util.Random;

public class GameModel {

        private int[][] grille = new int[6][6];
        private long seed; //la graine est creee
        private Random rand; //creation du random avec cette graine

    public GameModel() {
        seed = new Random().nextLong(); // on génère la valeur long, la graine en fait de la partie
        rand = new Random(seed);        // on crée le Random avec cette graine
        genererGrille();
    }

    public long getGraine(){return seed;}
    public int prochaineCouleur() { //c'est la mm graine qui sera reutilisee
            return rand.nextInt(7);
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
                        /*
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
                        }*/
                        boolean trop = false;
                        if (j >= 2 && grille[i][j-1] == couleur && grille[i][j-2] == couleur) trop = true;
                        if (i >= 2 && grille[i-1][j] == couleur && grille[i-2][j] == couleur) trop = true;
                        if (!trop) {
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

        public void decalerLigneGauche(int ligne) {
            int temp = grille[ligne][0];
            for (int j = 0; j < 5; j++)
                grille[ligne][j] = grille[ligne][j + 1];
            grille[ligne][5] = temp;
        }

        public void decalerColonneHaut(int colonne) {
            int temp = grille[0][colonne];
            for (int i = 0; i < 5; i++)
                grille[i][colonne] = grille[i + 1][colonne];
            grille[5][colonne] = temp;
        }


    public void setCase(int i, int j, int val) {
        grille[i][j] = val;
    }

    /**
     * Retourne true si aucun glissement ne peut créer un alignement de 3+ elements
     */
    //copie temporaire pour manipuler la grille et ainsi verifier si il reste encore des coups possibles
    private int[][] copierGrille() {
        int[][] copie = new int[6][6];
        for (int i = 0; i < 6; i++)
            copie[i] = grille[i].clone();
        return copie;
    }


    //pareil que verifierAlignement mais sans modifier la grille
    private boolean aUnAlignement(int[][] g) {
        // Horizontal
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                if (g[i][j] == g[i][j - 1]) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        // Vertical
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                if (g[i][j] == g[i - 1][j]) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        return false;
    }

    public boolean aucunCoupPossible() {

        // Tester tous les decalages de ligne possible
        for (int i = 0; i < 6; i++) {
            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = copierGrille(); //on manipule une copie de la grille
                // Simuler un décalage droite de 'shift' cases
                for (int s = 0; s < shift; s++) {
                    int temp = copie[i][5];
                    for (int j = 5; j > 0; j--) copie[i][j] = copie[i][j - 1];
                    copie[i][0] = temp;
                }
                if (aUnAlignement(copie)) return false;
            }
        }

        // Tester tous les décalages de colonne possible
        for (int j = 0; j < 6; j++) {
            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = copierGrille();
                // Simuler un décalage bas de 'shift' cases
                for (int s = 0; s < shift; s++) {
                    int temp = copie[5][j];
                    for (int i = 5; i > 0; i--) copie[i][j] = copie[i - 1][j];
                    copie[0][j] = temp;
                }
                if (aUnAlignement(copie)) return false;
            }
        }

        return true; // aucun coup trouvé → partie terminée
    }

    }

