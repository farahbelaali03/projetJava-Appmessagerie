package ui;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Contrôleur de la fenêtre de connexion (login.fxml).
 * Appelé par MainApp quand la scène login.fxml est chargée.
 * @author [Ton prénom]
 */
public class LoginController {

    // Champs FXML
    @FXML private TextField champNomUtilisateur;
    @FXML private PasswordField champMotDePasse;
    @FXML private Button boutonConnecter;
    @FXML private Label labelErreur;
    @FXML private ProgressIndicator indicateurChargement;

    // Méthodes FXML

    @FXML
    public void initialize() {
        labelErreur.setVisible(false);
        indicateurChargement.setVisible(false);
        // Appuyer sur Entrée dans le champ mot de passe → connecter
        champMotDePasse.setOnAction(e -> seConnecter());
    }

    @FXML
    public void seConnecter() {
        String nom = champNomUtilisateur.getText().trim();
        String mdp = champMotDePasse.getText().trim();

        // Validation locale
        if (nom.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        // Désactiver l'UI pendant la tentative
        boutonConnecter.setDisable(true);
        indicateurChargement.setVisible(true);
        labelErreur.setVisible(false);

        // Connexion dans un thread séparé pour ne pas bloquer l'UI
        Thread t = new Thread(() -> {
            boolean succes = MainApp.client.connecter(nom, mdp);
            Platform.runLater(() -> {
                indicateurChargement.setVisible(false);
                if (succes) {
                    // La réponse CONNECT vient du serveur → géré dans surConnexionReussie()
                    System.out.println("[LoginController] Connexion TCP établie, attente réponse serveur...");
                } else {
                    boutonConnecter.setDisable(false);
                    afficherErreur("Impossible de joindre le serveur.\nVérifiez qu'il est lancé.");
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // Méthodes appelées par Client

    /**
     * Appelée par Client.gererReponseConnexion() via MainApp.getControleur().
     * Connexion acceptée → ouvrir le chat.
     */
    public void surConnexionReussie() {
        Platform.runLater(() -> MainApp.changerScene("chat.fxml"));
    }

    /**
     * Appelée par Client.gererEchecConnexion() via MainApp.getControleur().
     * Identifiants incorrects → afficher erreur.
     */
    public void surEchecConnexion() {
        Platform.runLater(() -> {
            boutonConnecter.setDisable(false);
            afficherErreur("Nom d'utilisateur ou mot de passe incorrect.");
        });
    }

    // Méthodes privées

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
    }
}
