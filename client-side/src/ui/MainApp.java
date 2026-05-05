package ui;

import client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    // Attributs statiques (partagés entre contrôleurs)
    public static Client client = new Client();
    private static ChatController controleur;
    private static Stage fenetrePrincipale;

    // Méthodes JavaFX

    @Override
    public void start(Stage stage) throws Exception {
        fenetrePrincipale = stage;
        changerScene("login.fxml");
        stage.setTitle("Messagerie ENSA Tétouan");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Méthodes publiques statiques

    public static void changerScene(String nomFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/ui/fxml/" + nomFxml));
            Parent racine = loader.load();

            // Récupérer le contrôleur si c'est le chat
            if ("chat.fxml".equals(nomFxml)) {
                controleur = loader.getController();
            }

            fenetrePrincipale.setScene(new Scene(racine));

        } catch (Exception e) {
            System.out.println("Erreur chargement scène : " + e.getMessage());
        }
    }

    public static ChatController getControleur() {
        return controleur;
    }
}
