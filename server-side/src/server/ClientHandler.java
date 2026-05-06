package server;

import database.CallDAO;
import database.MessageDAO;
import database.UserDAO;
import model.Message;
import model.TypeMessage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private UserDAO userDAO = new UserDAO();
    private MessageDAO messageDAO = new MessageDAO();
    private CallDAO callDAO = new CallDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            Message msg;

            while ((msg = (Message) in.readObject()) != null) {

                switch (msg.getType()) {

                    case CONNECT:
                        handleConnect(msg);
                        break;

                    case DISCONNECT:
                        handleDisconnect();
                        break;

                    case MESSAGE:
                        handleMessage(msg);
                        break;

                    case FILE:
                        handleFile(msg);
                        break;

                    case CALL_REQUEST:
                        handleCallRequest(msg);
                        break;

                    case CALL_ACCEPT:
                        handleCallAccept(msg);
                        break;

                    case CALL_REJECT:
                        handleCallReject(msg);
                        break;

                    case AUDIO:
                        handleAudio(msg);
                        break;

                    case VIDEO:
                        handleVideo(msg);
                        break;

                    case GET_USERS:
                        handleGetUsers();
                        break;

                    default:
                        break;
                }
            }

        } catch (Exception e) {
            handleDisconnect();
        }
    }

    // ════════════════════════════════
    // CHAIMAA — LOGIN / DECONNEXION
    // ════════════════════════════════

    private void handleConnect(Message msg) {

        this.username = msg.getExpediteur();
        String password = msg.getContenu();

        boolean ok = userDAO.login(username, password);

        if (ok) {
            Server.clientsConnectes.put(username, this);
            userDAO.updateStatus(username, true);

            System.out.println(username + " connecté.");

            envoyer(new Message("SERVER", username,
                    "LOGIN_OK", TypeMessage.CONNECT));

            broadcastUserList();

        } else {
            envoyer(new Message("SERVER", username,
                    "LOGIN_FAIL", TypeMessage.DISCONNECT));
        }
    }

    private void handleDisconnect() {

        if (username != null) {
            Server.clientsConnectes.remove(username);
            userDAO.updateStatus(username, false);

            System.out.println(username + " déconnecté.");
            broadcastUserList();
        }

        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Erreur fermeture socket");
        }
    }

    private void broadcastUserList() {

        List<String> liste = new ArrayList<>(Server.clientsConnectes.keySet());
        String contenu = String.join(",", liste);

        Message msg = new Message("SERVER", "ALL",
                contenu, TypeMessage.GET_USERS);

        for (ClientHandler h : Server.clientsConnectes.values()) {
            h.envoyer(msg);
        }
    }

    // ════════════════════════════════
    // AFNANE — MESSAGES / FICHIERS
    // ════════════════════════════════

    private void handleMessage(Message msg) {
        messageDAO.sauvegarderMessage(msg);
        ClientHandler destinataire = Server.clientsConnectes.get(msg.getDestinataire());
        if (destinataire != null) {
            destinataire.envoyer(msg);
        }
    }

    private void handleFile(Message msg) {
        messageDAO.sauvegarderMessage(msg);
        ClientHandler destinataire = Server.clientsConnectes.get(msg.getDestinataire());
        if (destinataire != null) {
            destinataire.envoyer(msg);
        }
    }

    // ════════════════════════════════
    // FARAH — AUDIO / VIDEO
    // ════════════════════════════════

    private void handleCallRequest(Message msg) {
        callDAO.sauvegarderAppel(msg.getExpediteur(), msg.getDestinataire());
        ClientHandler dest = Server.clientsConnectes.get(msg.getDestinataire());
        if (dest != null) {
            dest.envoyer(msg);
        } else {
            envoyer(new Message("SERVER", msg.getExpediteur(),
                    msg.getDestinataire(), TypeMessage.CALL_REJECT));
        }
    }

    private void handleCallAccept(Message msg) {
        callDAO.accepterAppel(msg.getDestinataire(), msg.getExpediteur());
        ClientHandler appelant = Server.clientsConnectes.get(msg.getDestinataire());
        if (appelant != null) appelant.envoyer(msg);
    }

    private void handleCallReject(Message msg) {
        callDAO.refuserAppel(msg.getDestinataire(), msg.getExpediteur());
        ClientHandler appelant = Server.clientsConnectes.get(msg.getDestinataire());
        if (appelant != null) appelant.envoyer(msg);
    }

    private void handleAudio(Message msg) {
        ClientHandler dest = Server.clientsConnectes.get(msg.getDestinataire());
        if (dest != null) dest.envoyer(msg);
    }

    private void handleVideo(Message msg) {
        ClientHandler dest = Server.clientsConnectes.get(msg.getDestinataire());
        if (dest != null) dest.envoyer(msg);
    }

    // ════════════════════════════════
    // AMAL — CONTACTS
    // ════════════════════════════════

    private void handleGetUsers() {
        broadcastUserList();
    }

    // ════════════════════════════════
    // UTILITAIRE
    // ════════════════════════════════

    public void envoyer(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (Exception e) {
            System.out.println("Erreur envoi : " + e.getMessage());
        }
    }
}