package media;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Classe utilitaire pour recevoir des données via UDP.
 * @author Farah
 */
public class UDPReceiver {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int TAILLE_BUFFER = 65000;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private DatagramSocket socket;
    private int port;
    private boolean actif;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public UDPReceiver(int port) throws Exception {
        this.port   = port;
        this.socket = new DatagramSocket(port);
        this.actif  = false;
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public byte[] recevoirDonnees() {
        try {
            byte[] buffer = new byte[TAILLE_BUFFER];
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquet);
            byte[] donnees = new byte[paquet.getLength()];
            System.arraycopy(paquet.getData(), 0, donnees, 0, paquet.getLength());
            return donnees;
        } catch (Exception e) {
            if (actif) System.out.println("Erreur UDPReceiver : " + e.getMessage());
            return null;
        }
    }

    public void demarrer() { actif = true; }

    public void fermer() {
        actif = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }

    public boolean estActif() { return actif; }
    public int getPort()      { return port;  }
}