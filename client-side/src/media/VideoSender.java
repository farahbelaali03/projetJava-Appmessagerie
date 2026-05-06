package media;

import client.Client;
import model.Message;
import model.TypeMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Capture l'écran (simulation caméra), compresse en JPEG et envoie via TCP.
 * @author Farah
 */
public class VideoSender {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int LARGEUR_CAPTURE = 320;
    private static final int HAUTEUR_CAPTURE = 240;
    private static final int FPS             = 15;
    private static final int DELAI_MS        = 1000 / FPS;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private Client client;
    private String destinataire;
    private boolean actif;
    private Thread threadCapture;
    private Robot robot;
    private Rectangle zoneCapture;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public VideoSender(Client client, String destinataire) {
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
        System.out.println("VideoSender démarré vers " + destinataire);
    }

    public void arreter() {
        actif = false;
        if (threadCapture != null) threadCapture.interrupt();
        System.out.println("VideoSender arrêté.");
    }

    // ■■ Méthodes privées ■■■■■■■■■■■■■■■■■■■■■■■■■■■■

    private void boucleCapture() {
        try {
            robot       = new Robot();
            zoneCapture = new Rectangle(0, 0, LARGEUR_CAPTURE, HAUTEUR_CAPTURE);
            while (actif) {
                long debut     = System.currentTimeMillis();
                byte[] frame   = capturerFrame();
                if (frame != null) envoyerFrame(frame);
                long attente   = DELAI_MS - (System.currentTimeMillis() - debut);
                if (attente > 0) Thread.sleep(attente);
            }
        } catch (InterruptedException e) {
            System.out.println("VideoSender interrompu.");
        } catch (Exception e) {
            System.out.println("Erreur VideoSender : " + e.getMessage());
        }
    }

    private byte[] capturerFrame() {
        try {
            BufferedImage image = robot.createScreenCapture(zoneCapture);
            BufferedImage redim = new BufferedImage(
                    LARGEUR_CAPTURE, HAUTEUR_CAPTURE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = redim.createGraphics();
            g2d.drawImage(image, 0, 0, LARGEUR_CAPTURE, HAUTEUR_CAPTURE, null);
            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(redim, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.out.println("Erreur capture frame : " + e.getMessage());
            return null;
        }
    }

    private void envoyerFrame(byte[] frameJpeg) {
        String frameBase64 = Base64.getEncoder().encodeToString(frameJpeg);
        client.envoyer(new Message(
                client.getNomUtilisateur(),
                destinataire,
                frameBase64,
                TypeMessage.VIDEO));
    }

    public boolean estActif() { return actif; }
}