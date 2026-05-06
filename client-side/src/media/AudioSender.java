package media;

import client.Client;
import model.Message;
import model.TypeMessage;

import javax.sound.sampled.*;
import java.util.Base64;

/**
 * Capture le microphone et envoie les chunks audio via TCP.
 * @author Farah
 */
public class AudioSender {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int     TAILLE_BUFFER = 1024;
    private static final float   SAMPLE_RATE   = 44100.0f;
    private static final int     SAMPLE_SIZE   = 16;
    private static final int     CANAUX        = 1;
    private static final boolean SIGNE         = true;
    private static final boolean BIG_ENDIAN    = false;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private Client client;
    private String destinataire;
    private boolean actif;
    private Thread threadCapture;
    private TargetDataLine ligne;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public AudioSender(Client client, String destinataire) {
        this.client       = client;
        this.destinataire = destinataire;
        this.actif        = false;
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void demarrer() {
        actif = true;
        threadCapture = new Thread(this::boucleCapture);
        threadCapture.setDaemon(true);
        threadCapture.start();
        System.out.println("AudioSender démarré vers " + destinataire);
    }

    public void arreter() {
        actif = false;
        if (ligne != null && ligne.isOpen()) { ligne.stop(); ligne.close(); }
        if (threadCapture != null) threadCapture.interrupt();
        System.out.println("AudioSender arrêté.");
    }

    // ■■ Méthodes privées ■■■■■■■■■■■■■■■■■■■■■■■■■■■■

    private void boucleCapture() {
        try {
            AudioFormat format = new AudioFormat(
                    SAMPLE_RATE, SAMPLE_SIZE, CANAUX, SIGNE, BIG_ENDIAN);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Microphone non supporté.");
                return;
            }
            ligne = (TargetDataLine) AudioSystem.getLine(info);
            ligne.open(format);
            ligne.start();
            byte[] buffer = new byte[TAILLE_BUFFER];
            while (actif) {
                int octetsLus = ligne.read(buffer, 0, buffer.length);
                if (octetsLus > 0) envoyerChunk(buffer, octetsLus);
            }
        } catch (Exception e) {
            System.out.println("Erreur AudioSender : " + e.getMessage());
        }
    }

    private void envoyerChunk(byte[] buffer, int longueur) {
        byte[] chunk = new byte[longueur];
        System.arraycopy(buffer, 0, chunk, 0, longueur);
        String base64 = Base64.getEncoder().encodeToString(chunk);
        client.envoyer(new Message(
                client.getNomUtilisateur(),
                destinataire,
                base64,
                TypeMessage.AUDIO));
    }

    public boolean estActif() { return actif; }
}