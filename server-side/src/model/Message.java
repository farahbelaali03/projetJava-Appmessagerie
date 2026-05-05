package model;

import java.io.Serializable;

public class Message implements Serializable {

    private String expediteur;
    private String destinataire;
    private String contenu;
    private TypeMessage type;

    // Constructeur
    public Message(String expediteur, String destinataire,
                   String contenu, TypeMessage type) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.contenu = contenu;
        this.type = type;
    }

    // Getters
    public String getExpediteur() {
        return expediteur;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public String getContenu() {
        return contenu;
    }

    public TypeMessage getType() {
        return type;
    }

    // Setters (optionnel)
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}