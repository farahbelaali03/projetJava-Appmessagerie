package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    // Port du serveur
    public static final int PORT = 5000;

    // Liste des clients connectés (username → ClientHandler)
    public static final ConcurrentHashMap<String, ClientHandler> clientsConnectes = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        System.out.println("Serveur démarré sur le port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("Nouveau client connecté : "
                        + socket.getInetAddress().getHostAddress());

                // Création du handler pour ce client
                ClientHandler handler = new ClientHandler(socket);

                // Lancement dans un thread
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println("Erreur serveur !");
            e.printStackTrace();
        }
    }
}