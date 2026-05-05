package client;

import model.Message;
import model.TypeMessage;
import ui.MainApp;

import java.io.*;
import java.net.Socket;


public class Client {

    // Constantes
    private static final String ADRESSE_SERVEUR = "localhost";
    private static final int PORT = 5000;

    // Attributs
    private Socket socket;
    private ObjectOutputStream fluxSortie;
    private ObjectInputStream fluxEntree;
    private String nomUtilisateur;
    private boolean connecte = false;

    // Constructeur
    public Client() { }

    // Méthodes publiques

    public boolean connecter(String nomUtilisateur, String motDePasse) {
        try {
            this.nomUtilisateur = nomUtilisateur;
            socket = new Socket(ADRESSE_SERVEUR, PORT);
            fluxSortie = new ObjectOutputStream(socket.getOutputStream());
            fluxEntree  = new ObjectInputStream(socket.getInputStream());
            connecte = true;

            // Envoyer la demande de connexion
            envoyer(new Message(nomUtilisateur, "SERVER",
                    motDePasse, TypeMessage.CONNECT));

            // Lancer l'écoute des messages entrants
            demarrerEcoute();
            return true;

        } catch (IOException e) {
            System.out.println("Erreur connexion : " + e.getMessage());
            return false;
        }
    }

    public void deconnecter() {
        connecte = false;
        envoyer(new Message(nomUtilisateur, "SERVER",
                "", TypeMessage.DISCONNECT));
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Erreur déconnexion : " + e.getMessage());
        }
    }

    public void envoyerMessage(String destinataire, String contenu) {
        envoyer(new Message(nomUtilisateur, destinataire,
                contenu, TypeMessage.MESSAGE));
    }

    public void demanderAppel(String destinataire) {
        envoyer(new Message(nomUtilisateur, destinataire,
                "", TypeMessage.CALL_REQUEST));
    }

    public void accepterAppel(String destinataire) {
        envoyer(new Message(nomUtilisateur, destinataire,
                "", TypeMessage.CALL_ACCEPT));
    }

    public void refuserAppel(String destinataire) {
        envoyer(new Message(nomUtilisateur, destinataire,
                "", TypeMessage.CALL_REJECT));
    }

    public void demanderListeUtilisateurs() {
        envoyer(new Message(nomUtilisateur, "SERVER",
                "", TypeMessage.GET_USERS));
    }

    public void envoyer(Message message) {
        try {
            fluxSortie.writeObject(message);
            fluxSortie.flush();
            fluxSortie.reset();
        } catch (IOException e) {
            System.out.println("Erreur envoi : " + e.getMessage());
        }
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public boolean estConnecte() {
        return connecte;
    }

    //  Méthodes privées

    private void demarrerEcoute() {
        Thread threadEcoute = new Thread(() -> {
            try {
                Message message;
                while (connecte &&
                        (message = (Message) fluxEntree.readObject()) != null) {
                    traiterMessageRecu(message);
                }
            } catch (Exception e) {
                if (connecte) {
                    System.out.println("Connexion perdue : " + e.getMessage());
                    connecte = false;
                }
            }
        });
        threadEcoute.setDaemon(true);
        threadEcoute.start();
    }

    private void traiterMessageRecu(Message message) {
        switch (message.getType()) {

            case CONNECT:
                gererReponseConnexion(message);
                break;

            case DISCONNECT:
                gererEchecConnexion(message);
                break;

            case MESSAGE:
                MainApp.getControleur().afficherMessage(message);
                break;

            case FILE:
                MainApp.getControleur().afficherFichier(message);
                break;

            case GET_USERS:
                MainApp.getControleur().mettreAJourListeUtilisateurs(
                        message.getContenu());
                break;

            case CALL_REQUEST:
                MainApp.getControleur().afficherDemandeAppel(message);
                break;

            case CALL_ACCEPT:
                MainApp.getControleur().gererAcceptationAppel(message);
                break;

            case CALL_REJECT:
                MainApp.getControleur().gererRefusAppel(message);
                break;

            default:
                break;
        }
    }

    private void gererReponseConnexion(Message message) {
        if ("LOGIN_OK".equals(message.getContenu())) {
            System.out.println("Connexion acceptée par le serveur.");
            MainApp.getControleur().surConnexionReussie();
        }
    }

    private void gererEchecConnexion(Message message) {
        if ("LOGIN_FAIL".equals(message.getContenu())) {
            connecte = false;
            System.out.println("Identifiants incorrects.");
            MainApp.getControleur().surEchecConnexion();
        }
    }
}