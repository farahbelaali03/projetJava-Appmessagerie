package ui;

import client.Client;
import model.Message;
import model.TypeMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Contrôleur principal du chat (chat.fxml).
 * Gère : liste des contacts, messages texte, fichiers, appels.
 * @author [Ton prénom]
 */
public class ChatController {

    // Champs FXML
    @FXML private Label labelNomUtilisateur;
    @FXML private VBox listeContacts;
    @FXML private VBox zoneMessages;
    @FXML private ScrollPane scrollMessages;
    @FXML private TextField champMessage;
    @FXML private Button boutonEnvoyer;
    @FXML private Button boutonFichier;
    @FXML private Button boutonAppelVideo;
    @FXML private Label labelContactActif;
    @FXML private Label labelStatutContact;

    // Attributs
    private String contactActif = null;
    // Messages reçus pendant qu'une autre conversation est ouverte
    private final Map<String, List<Message>> messagesEnAttente = new HashMap<>();

    private static final String[] COULEURS = {
            "#3C3489","#712B13","#0F6E56","#0C447C",
            "#633806","#1D6B3A","#7B2D8B","#8B4513"
    };

    // Initialisation FXML

    @FXML
    public void initialize() {
        // Afficher le nom de l'utilisateur connecté
        labelNomUtilisateur.setText(MainApp.client.getNomUtilisateur());

        // Envoyer avec la touche Entrée
        champMessage.setOnAction(e -> envoyerMessage());

        // Auto-scroll vers le bas quand de nouveaux messages arrivent
        zoneMessages.heightProperty().addListener((obs, o, n) ->
                scrollMessages.setVvalue(1.0)
        );

        // Demander la liste des utilisateurs connectés au serveur
        MainApp.client.demanderListeUtilisateurs();
    }

    // Actions FXML

    @FXML
    public void envoyerMessage() {
        if (contactActif == null) return;
        String texte = champMessage.getText().trim();
        if (texte.isEmpty()) return;

        // Afficher dans l'UI côté envoyeur
        String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        ajouterBubble(texte, true, MainApp.client.getNomUtilisateur(), heure);
        champMessage.clear();

        // Envoyer via le réseau
        MainApp.client.envoyerMessage(contactActif, texte);
    }

    @FXML
    public void envoyerFichier() {
        if (contactActif == null) {
            afficherAlerte("Sélectionnez d'abord un contact.");
            return;
        }
        FileChooser selecteur = new FileChooser();
        selecteur.setTitle("Choisir un fichier à envoyer");
        File fichier = selecteur.showOpenDialog(null);
        if (fichier == null) return;

        try {
            byte[] octets = Files.readAllBytes(fichier.toPath());
            // Encoder en Base64 pour transporter dans le champ contenu
            String contenuEncode = java.util.Base64.getEncoder().encodeToString(octets);
            // Format : "nomFichier||contenuBase64"
            String contenu = fichier.getName() + "||" + contenuEncode;

            Message msg = new Message(
                    MainApp.client.getNomUtilisateur(),
                    contactActif,
                    contenu,
                    TypeMessage.FILE
            );
            MainApp.client.envoyer(msg);

            String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            ajouterBubbleFichier(fichier.getName(), true, heure);

        } catch (IOException e) {
            afficherAlerte("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
    }

    @FXML
    public void lancerAppelVideo() {
        if (contactActif == null) {
            afficherAlerte("Sélectionnez d'abord un contact.");
            return;
        }
        MainApp.client.demanderAppel(contactActif);
        afficherAlerte("Appel envoyé à " + contactActif + "...");
    }

    @FXML
    public void seDeconnecter() {
        MainApp.client.deconnecter();
        MainApp.changerScene("login.fxml");
    }

    // Méthodes appelées par Client
    // Ces méthodes correspondent exactement aux appels dans Client.traiterMessageRecu()

    /**
     * Client.java ligne : MainApp.getControleur().afficherMessage(message)
     * Reçoit un message TEXT entrant.
     */
    public void afficherMessage(Message message) {
        Platform.runLater(() -> {
            String expediteur = message.getExpediteur();
            String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            if (expediteur.equals(contactActif)) {
                // La conversation est ouverte → afficher directement
                ajouterBubble(message.getContenu(), false, expediteur, heure);
            } else {
                // Stocker en attente + notifier dans la liste
                messagesEnAttente.computeIfAbsent(expediteur, k -> new ArrayList<>())
                        .add(message);
                mettreAJourBadgeContact(expediteur);
            }
        });
    }

    /**
     * Client.java ligne : MainApp.getControleur().afficherFichier(message)
     * Reçoit un fichier entrant.
     */
    public void afficherFichier(Message message) {
        Platform.runLater(() -> {
            String expediteur = message.getExpediteur();
            // Extraire le nom du fichier
            String[] parties = message.getContenu().split("\\|\\|", 2);
            String nomFichier = parties.length > 0 ? parties[0] : "fichier";
            String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            // Sauvegarder le fichier reçu
            if (parties.length == 2) {
                sauvegarderFichierRecu(nomFichier, parties[1]);
            }

            if (expediteur.equals(contactActif)) {
                ajouterBubbleFichier(nomFichier, false, heure);
            } else {
                messagesEnAttente.computeIfAbsent(expediteur, k -> new ArrayList<>())
                        .add(message);
                mettreAJourBadgeContact(expediteur);
            }
        });
    }

    /**
     * Client.java ligne : MainApp.getControleur().mettreAJourListeUtilisateurs(contenu)
     * Le contenu est une chaîne de noms séparés par des virgules : "alice,bob,charlie"
     */
    public void mettreAJourListeUtilisateurs(String contenu) {
        Platform.runLater(() -> {
            listeContacts.getChildren().clear();
            if (contenu == null || contenu.isBlank()) return;

            String[] utilisateurs = contenu.split(",");
            for (String nom : utilisateurs) {
                nom = nom.trim();
                if (!nom.isEmpty() && !nom.equals(MainApp.client.getNomUtilisateur())) {
                    listeContacts.getChildren().add(creerLigneContact(nom, true));
                }
            }
        });
    }

    /**
     * Client.java ligne : MainApp.getControleur().afficherDemandeAppel(message)
     * Quelqu'un demande un appel vidéo.
     */
    public void afficherDemandeAppel(Message message) {
        Platform.runLater(() -> {
            String appelant = message.getExpediteur();
            Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
            alerte.setTitle("Appel entrant");
            alerte.setHeaderText(appelant + " vous appelle");
            alerte.setContentText("Voulez-vous accepter l'appel vidéo ?");

            ButtonType btnAccepter = new ButtonType("Accepter", ButtonBar.ButtonData.YES);
            ButtonType btnRefuser  = new ButtonType("Refuser",  ButtonBar.ButtonData.NO);
            alerte.getButtonTypes().setAll(btnAccepter, btnRefuser);

            alerte.showAndWait().ifPresent(reponse -> {
                if (reponse == btnAccepter) {
                    MainApp.client.accepterAppel(appelant);
                    ouvrirFenetreVideoCall(appelant);
                } else {
                    MainApp.client.refuserAppel(appelant);
                }
            });
        });
    }

    /**
     * Client.java ligne : MainApp.getControleur().gererAcceptationAppel(message)
     * L'interlocuteur a accepté notre appel.
     */
    public void gererAcceptationAppel(Message message) {
        Platform.runLater(() -> {
            ouvrirFenetreVideoCall(message.getExpediteur());
        });
    }

    /**
     * Client.java ligne : MainApp.getControleur().gererRefusAppel(message)
     * L'interlocuteur a refusé notre appel.
     */
    public void gererRefusAppel(Message message) {
        Platform.runLater(() ->
                afficherAlerte(message.getExpediteur() + " a refusé votre appel.")
        );
    }

    /**
     * Connexion réussie → déjà géré par LoginController mais peut être appelé ici aussi.
     */
    public void surConnexionReussie() {
        // Rien à faire ici, LoginController.surConnexionReussie() change la scène
    }

    /**
     * Échec connexion → ne devrait pas arriver depuis ChatController.
     */
    public void surEchecConnexion() {
        Platform.runLater(() -> MainApp.changerScene("login.fxml"));
    }

    // Méthodes privées UI

    private void ouvrirContact(String nom) {
        contactActif = nom;
        labelContactActif.setText(nom);
        labelStatutContact.setText("En ligne");
        labelStatutContact.setStyle("-fx-text-fill: #25D366;");
        zoneMessages.getChildren().clear();
        boutonAppelVideo.setDisable(false);
        champMessage.setDisable(false);
        boutonEnvoyer.setDisable(false);
        boutonFichier.setDisable(false);

        // Afficher les messages en attente pour ce contact
        List<Message> enAttente = messagesEnAttente.remove(nom);
        if (enAttente != null) {
            for (Message m : enAttente) {
                String heure = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                if (m.getType() == TypeMessage.FILE) {
                    String[] p = m.getContenu().split("\\|\\|", 2);
                    ajouterBubbleFichier(p.length > 0 ? p[0] : "fichier", false, heure);
                } else {
                    ajouterBubble(m.getContenu(), false, m.getExpediteur(), heure);
                }
            }
        }
        // Retirer le badge de notification
        rafraichirLigneContact(nom, false);
    }

    private HBox creerLigneContact(String nom, boolean enLigne) {
        String ini   = initiales(nom);
        String couleur = couleur(nom);

        // Avatar
        Label avatar = new Label(ini);
        avatar.setMinSize(42, 42);
        avatar.setMaxSize(42, 42);
        avatar.setAlignment(Pos.CENTER);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        avatar.setTextFill(Color.WHITE);
        avatar.setStyle("-fx-background-color:" + couleur + "; -fx-background-radius:21px;");

        // Nom + statut
        Label labelNom = new Label(nom);
        labelNom.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        labelNom.setStyle("-fx-text-fill: #E9EDEF;");

        // Badge notification
        int nbEnAttente = messagesEnAttente.containsKey(nom)
                ? messagesEnAttente.get(nom).size() : 0;
        Label badge = new Label(nbEnAttente > 0 ? String.valueOf(nbEnAttente) : "");
        badge.setVisible(nbEnAttente > 0);
        badge.setMinSize(20, 20);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: #25D366; -fx-background-radius: 10px; " +
                "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label statutLbl = new Label(enLigne ? "● En ligne" : "○ Hors ligne");
        statutLbl.setFont(Font.font("Arial", 11));
        statutLbl.setStyle("-fx-text-fill: " + (enLigne ? "#25D366" : "#8696A0") + ";");

        VBox infoBox = new VBox(2, labelNom, statutLbl);
        HBox ligne = new HBox(10, avatar, infoBox, new Pane(), badge);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(10, 12, 10, 12));
        ligne.setStyle("-fx-cursor: hand;");
        ligne.setUserData(nom); // pour retrouver la ligne par nom

        ligne.setOnMouseEntered(e ->
                ligne.setStyle("-fx-background-color:#202C33; -fx-cursor:hand;"));
        ligne.setOnMouseExited(e ->
                ligne.setStyle("-fx-background-color:transparent; -fx-cursor:hand;"));
        ligne.setOnMouseClicked(e -> ouvrirContact(nom));

        return ligne;
    }

    private void ajouterBubble(String contenu, boolean estSortant, String expediteur, String heure) {
        String texte = "[" + heure + "] " + expediteur + " : " + contenu;
        if (estSortant) texte += "  ✓✓";

        Label bulle = new Label(texte);
        bulle.setWrapText(true);
        bulle.setMaxWidth(420);
        bulle.setFont(Font.font("Arial", 13));
        bulle.setPadding(new Insets(7, 10, 7, 10));
        bulle.setStyle("-fx-background-color:" + (estSortant ? "#005C4B" : "#202C33")
                + "; -fx-background-radius:8px; -fx-text-fill:#E9EDEF;");

        HBox rangee = new HBox(bulle);
        rangee.setAlignment(estSortant ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        rangee.setPadding(new Insets(2, 20, 2, 20));
        zoneMessages.getChildren().add(rangee);
    }

    private void ajouterBubbleFichier(String nomFichier, boolean estSortant, String heure) {
        String texte = "📎 [" + heure + "] " + nomFichier;
        if (estSortant) texte += "  ✓✓";

        Label bulle = new Label(texte);
        bulle.setWrapText(true);
        bulle.setMaxWidth(420);
        bulle.setFont(Font.font("Arial", 13));
        bulle.setPadding(new Insets(7, 10, 7, 10));
        bulle.setStyle("-fx-background-color:" + (estSortant ? "#005C4B" : "#1A3A4A")
                + "; -fx-background-radius:8px; -fx-text-fill:#E9EDEF; -fx-cursor:hand;");

        if (!estSortant) {
            bulle.setOnMouseClicked(e -> ouvrirFichierRecu(nomFichier));
        }

        HBox rangee = new HBox(bulle);
        rangee.setAlignment(estSortant ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        rangee.setPadding(new Insets(2, 20, 2, 20));
        zoneMessages.getChildren().add(rangee);
    }

    private void mettreAJourBadgeContact(String nom) {
        listeContacts.getChildren().stream()
                .filter(n -> nom.equals(n.getUserData()))
                .findFirst()
                .ifPresent(n -> {
                    // Recréer la ligne pour mettre à jour le badge
                    int index = listeContacts.getChildren().indexOf(n);
                    listeContacts.getChildren().set(index, creerLigneContact(nom, true));
                });
    }

    private void rafraichirLigneContact(String nom, boolean avecBadge) {
        listeContacts.getChildren().stream()
                .filter(n -> nom.equals(n.getUserData()))
                .findFirst()
                .ifPresent(n -> {
                    int index = listeContacts.getChildren().indexOf(n);
                    listeContacts.getChildren().set(index, creerLigneContact(nom, true));
                });
    }

    private void ouvrirFenetreVideoCall(String interlocuteur) {
        // VideoCallWindow est en Swing (Farah), on l'ouvre dans un thread Swing
        javax.swing.SwingUtilities.invokeLater(() ->
                new VideoCallWindow(MainApp.client, interlocuteur)
        );
    }

    private void sauvegarderFichierRecu(String nomFichier, String contenuBase64) {
        try {
            byte[] octets = java.util.Base64.getDecoder().decode(contenuBase64);
            File dossier = new File("received_files");
            if (!dossier.exists()) dossier.mkdirs();
            File destination = new File(dossier, nomFichier);
            Files.write(destination.toPath(), octets);
            System.out.println("[ChatController] Fichier sauvegardé : " + destination.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("[ChatController] Erreur sauvegarde fichier : " + e.getMessage());
        }
    }

    private void ouvrirFichierRecu(String nomFichier) {
        try {
            File fichier = new File("received_files/" + nomFichier);
            if (fichier.exists()) {
                java.awt.Desktop.getDesktop().open(fichier);
            } else {
                afficherAlerte("Fichier introuvable : " + nomFichier);
            }
        } catch (Exception e) {
            afficherAlerte("Impossible d'ouvrir le fichier.");
        }
    }

    private void afficherAlerte(String message) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle("Information");
        alerte.setHeaderText(null);
        alerte.setContentText(message);
        alerte.show();
    }

    // Helpers

    private String initiales(String nom) {
        String[] p = nom.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return ("" + p[0].charAt(0) + p[1].charAt(0)).toUpperCase();
    }

    private String couleur(String nom) {
        return COULEURS[Math.abs(nom.hashCode()) % COULEURS.length];
    }
}
