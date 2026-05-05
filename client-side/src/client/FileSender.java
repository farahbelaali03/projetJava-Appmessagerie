package client;

import model.Message;
import model.TypeMessage;

import java.io.*;
import java.nio.file.Files;

public class FileSender {

    // Constantes
    private static final int TAILLE_MAX_FICHIER = 10 * 1024 * 1024; // 10 MB

    //  Attributs
    private final Client client;

    //  Constructeur
    public FileSender(Client client) {
        this.client = client;
    }

    //  Méthodes publiques

    public boolean envoyerFichier(String destinataire, File fichier) {
        if (!validerFichier(fichier)) return false;

        try {
            byte[] donnees = Files.readAllBytes(fichier.toPath());
            String contenu = fichier.getName() + "|"
                    + java.util.Base64.getEncoder().encodeToString(donnees);

            client.envoyer(new Message(
                    client.getNomUtilisateur(),
                    destinataire,
                    contenu,
                    TypeMessage.FILE
            ));
            return true;

        } catch (IOException e) {
            System.out.println("Erreur lecture fichier : " + e.getMessage());
            return false;
        }
    }

    public static void sauvegarderFichierRecu(String contenu,
                                              String dossierDestination) {
        try {
            String[] parties = contenu.split("\\|", 2);
            if (parties.length < 2) return;

            String nomFichier = parties[0];
            byte[] donnees = java.util.Base64.getDecoder().decode(parties[1]);

            File dossier = new File(dossierDestination);
            if (!dossier.exists()) dossier.mkdirs();

            File fichierSortie = new File(dossier, nomFichier);
            Files.write(fichierSortie.toPath(), donnees);
            System.out.println("Fichier sauvegardé : " + fichierSortie.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("Erreur sauvegarde fichier : " + e.getMessage());
        }
    }

    //  Méthodes privées

    private boolean validerFichier(File fichier) {
        if (fichier == null || !fichier.exists()) {
            System.out.println("Fichier introuvable.");
            return false;
        }
        if (fichier.length() > TAILLE_MAX_FICHIER) {
            System.out.println("Fichier trop volumineux (max 10 MB).");
            return false;
        }
        return true;
    }
}