package media;

import model.Message;

import javax.sound.sampled.*;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reçoit les chunks audio et les joue avec SourceDataLine.
 * @author Farah
 */
public class AudioReceiver {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final float   SAMPLE_RATE = 44100.0f;
    private static final int     SAMPLE_SIZE = 16;
    private static final int     CANAUX      = 1;
    private static final boolean SIGNE       = true;
    private static final boolean BIG_ENDIAN  = false;

    // ■■ Attributs ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private boolean actif;
    private Thread threadLecture;
    private SourceDataLine ligne;
    private BlockingQueue<byte[]> fileChunks;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public AudioReceiver() {
        this.actif      = false;
        this.fileChunks = new LinkedBlockingQueue<>(20);
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void demarrer() {
        try {
            AudioFormat format = new AudioFormat(
                    SAMPLE_RATE, SAMPLE_SIZE, CANAUX, SIGNE, BIG_ENDIAN);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Haut-parleur non supporté.");
                return;
            }
            ligne = (SourceDataLine) AudioSystem.getLine(info);
            ligne.open(format);
            ligne.start();
            actif = true;
            threadLecture = new Thread(this::boucleLecture);
            threadLecture.setDaemon(true);
            threadLecture.start();
            System.out.println("AudioReceiver démarré.");
        } catch (Exception e) {
            System.out.println("Erreur AudioReceiver.demarrer : " + e.getMessage());
        }
    }

    public void arreter() {
        actif = false;
        if (threadLecture != null) threadLecture.interrupt();
        if (ligne != null && ligne.isOpen()) { ligne.drain(); ligne.stop(); ligne.close(); }
        System.out.println("AudioReceiver arrêté.");
    }

    public void recevoirChunk(Message msg) {
        try {
            byte[] chunk = Base64.getDecoder().decode(msg.getContenu());
            fileChunks.offer(chunk);
        } catch (Exception e) {
            System.out.println("Erreur décodage chunk audio : " + e.getMessage());
        }
    }

    // ■■ Méthodes privées ■■■■■■■■■■■■■■■■■■■■■■■■■■■■

    private void boucleLecture() {
        while (actif) {
            try {
                byte[] chunk = fileChunks.take();
                ligne.write(chunk, 0, chunk.length);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean estActif() { return actif; }
}