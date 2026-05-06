package media;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Classe utilitaire pour envoyer des données via UDP.
 * @author Farah
 */
public class UDPSender {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int TAILLE_MAX_PAQUET = 65000;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private DatagramSocket socket;
    private InetAddress adresse;
    private int port;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public UDPSender(String adresseServeur, int port) throws Exception {
        this.socket  = new DatagramSocket();
        this.adresse = InetAddress.getByName(adresseServeur);
        this.port    = port;
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void envoyerDonnees(byte[] donnees) {
        try {
            if (donnees.length <= TAILLE_MAX_PAQUET) {
                DatagramPacket paquet = new DatagramPacket(
                        donnees, donnees.length, adresse, port);
                socket.send(paquet);
            } else {
                System.out.println("Paquet trop grand : " + donnees.length + " bytes");
            }
        } catch (Exception e) {
            System.out.println("Erreur UDPSender.envoyerDonnees : " + e.getMessage());
        }
    }

    public void fermer() {
        if (socket != null && !socket.isClosed()) socket.close();
    }

    public boolean estOuvert() {
        return socket != null && !socket.isClosed();
    }
}