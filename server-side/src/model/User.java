package model;

import java.io.Serializable;

    /**
     * Classe représentant un utilisateur de l'application.
     * @author Afnane
     */
    public class User implements Serializable {

        // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        private static final long serialVersionUID = 1L;

        // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        private int id;
        private String nomUtilisateur;
        private String motDePasse;
        private boolean enLigne;

        // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        public User(int id, String nomUtilisateur, boolean enLigne) {
            this.id = id;
            this.nomUtilisateur = nomUtilisateur;
            this.enLigne = enLigne;
        }

        public User(String nomUtilisateur, String motDePasse) {
            this.nomUtilisateur = nomUtilisateur;
            this.motDePasse = motDePasse;
        }

        // ■■ Getters / Setters ■■■■■■■■■■■■■■■■■■■■■■■■■■■
        public int getId() { return id; }

        public String getNomUtilisateur() { return nomUtilisateur; }

        public String getMotDePasse() { return motDePasse; }

        public boolean isEnLigne() { return enLigne; }

        public void setEnLigne(boolean enLigne) { this.enLigne = enLigne; }

        @Override
        public String toString() {
            return nomUtilisateur + (enLigne ? " (en ligne)" : " (hors ligne)");
        }
    }
