package ui;

import client.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Message;
import model.TypeMessage;

/**
 * Contrôleur de la fenêtre d'appel entrant (call.fxml).
 * Affiche qui appelle et permet d'accepter ou refuser.
 * @author Amal
 */
public class CallController {

    // Champs FXML
    @FXML private Label labelAppelant;
    @FXML private Label labelStatut;

    // Attributs
    private String nomAppelant;
    private Stage fenetreAppel;

    // Initialisation
    @FXML
    public void initialize() {
        labelStatut.setText("Appel vidéo entrant...");
    }

    // Méthodes publiques

    /**
     * Appelée par ChatController pour configurer la fenêtre.
     */
    public void setAppelant(String nomAppelant, Stage fenetreAppel) {
        this.nomAppelant    = nomAppelant;
        this.fenetreAppel   = fenetreAppel;
        labelAppelant.setText(nomAppelant);
    }

    // Actions FXML

    @FXML
    public void accepterAppel() {
        MainApp.client.accepterAppel(nomAppelant);
        fermerFenetre();
        // VideoCallWindow sera ouverte par ChatController.gererAcceptationAppel()
    }

    @FXML
    public void refuserAppel() {
        MainApp.client.refuserAppel(nomAppelant);
        fermerFenetre();
    }

    //  Méthodes privées
    private void fermerFenetre() {
        if (fenetreAppel != null) {
            Platform.runLater(fenetreAppel::close);
        }
    }
}