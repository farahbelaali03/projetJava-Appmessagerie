package media;

import model.Message;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reçoit les frames JPEG et les affiche dans un JLabel.
 * @author Farah
 */
public class VideoReceiver {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int LARGEUR_AFFICHAGE = 320;
    private static final int HAUTEUR_AFFICHAGE = 240;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private JLabel labelAffichage;
    private boolean actif;
    private Thread threadAffichage;
    private BlockingQueue<byte[]> fileFrames;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public VideoReceiver(JLabel labelAffichage) {
        this.labelAffichage = labelAffichage;
        this.actif          = false;
        this.fileFrames     = new LinkedBlockingQueue<>(5);
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void demarrer() {
        actif = true;
        threadAffichage = new Thread(this::boucleAffichage);
        threadAffichage.setDaemon(true);
        threadAffichage.start();
        System.out.println("VideoReceiver démarré.");
    }

    public void arreter() {
        actif = false;
        if (threadAffichage != null) threadAffichage.interrupt();
        SwingUtilities.invokeLater(() -> labelAffichage.setIcon(null));
        System.out.println("VideoReceiver arrêté.");
    }

    public void recevoirFrame(Message msg) {
        try {
            byte[] frameJpeg = Base64.getDecoder().decode(msg.getContenu());
            fileFrames.offer(frameJpeg);
        } catch (Exception e) {
            System.out.println("Erreur décodage frame : " + e.getMessage());
        }
    }

    // ■■ Méthodes privées ■■■■■■■■■■■■■■■■■■■■■■■■■■■■

    private void boucleAffichage() {
        while (actif) {
            try {
                byte[] frameJpeg = fileFrames.take();
                afficherFrame(frameJpeg);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void afficherFrame(byte[] frameJpeg) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(frameJpeg));
            if (image != null) {
                Image redim = image.getScaledInstance(
                        LARGEUR_AFFICHAGE, HAUTEUR_AFFICHAGE, Image.SCALE_FAST);
                SwingUtilities.invokeLater(
                        () -> labelAffichage.setIcon(new ImageIcon(redim)));
            }
        } catch (Exception e) {
            System.out.println("Erreur affichage frame : " + e.getMessage());
        }
    }

    public boolean estActif() { return actif; }
}