package ui;

import client.Client;
import media.AudioReceiver;
import media.AudioSender;
import media.VideoReceiver;
import media.VideoSender;
import model.Message;
import model.TypeMessage;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre d'appel vidéo : deux zones vidéo, bouton raccrocher, bouton mute.
 * @author Farah
 */
public class VideoCallWindow extends JFrame {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final int LARGEUR_FENETRE  = 760;
    private static final int HAUTEUR_FENETRE  = 560;
    private static final int LARGEUR_VIDEO    = 320;
    private static final int HAUTEUR_VIDEO    = 240;

    // ■■ Attributs UI ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private JLabel labelVideoLocal;
    private JLabel labelVideoDistant;
    private JButton boutonRaccrocher;
    private JButton boutonMute;
    private JLabel labelStatut;

    // ■■ Attributs Médias ■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private VideoSender videoSender;
    private VideoReceiver videoReceiver;
    private AudioSender audioSender;
    private AudioReceiver audioReceiver;
    private Client client;
    private String interlocuteur;
    private boolean muet;

    // ■■ Constructeur ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public VideoCallWindow(Client client, String interlocuteur) {
        this.client        = client;
        this.interlocuteur = interlocuteur;
        this.muet          = false;
        initialiserUI();
        demarrerMedia();
    }

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void afficherFrameDistante(Message msg) {
        if (videoReceiver != null) videoReceiver.recevoirFrame(msg);
    }

    public void jouerAudioDistant(Message msg) {
        if (audioReceiver != null) audioReceiver.recevoirChunk(msg);
    }

    public void terminerAppel() {
        arreterMedia();
        SwingUtilities.invokeLater(this::dispose);
    }

    // ■■ Méthodes privées UI ■■■■■■■■■■■■■■■■■■■■■■■■■

    private void initialiserUI() {
        setTitle("Appel vidéo avec " + interlocuteur);
        setSize(LARGEUR_FENETRE, HAUTEUR_FENETRE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout(10, 10));

        add(creerPanneauVideos(),  BorderLayout.CENTER);
        add(creerPanneauBoutons(), BorderLayout.SOUTH);

        labelStatut = new JLabel("Appel en cours...", SwingConstants.CENTER);
        labelStatut.setForeground(Color.WHITE);
        labelStatut.setFont(new Font("Arial", Font.PLAIN, 13));
        labelStatut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(labelStatut, BorderLayout.NORTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) { raccrocher(); }
        });

        setVisible(true);
    }

    private JPanel creerPanneauVideos() {
        JPanel panneau = new JPanel(new GridLayout(1, 2, 10, 0));
        panneau.setBackground(new Color(30, 30, 30));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panneau.add(creerZoneVideo(false)); // interlocuteur
        panneau.add(creerZoneVideo(true));  // soi-même
        return panneau;
    }

    private JPanel creerZoneVideo(boolean estLocal) {
        JPanel panneau = new JPanel(new BorderLayout(0, 5));
        panneau.setBackground(new Color(50, 50, 50));
        panneau.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));

        JLabel labelVideo = new JLabel("", SwingConstants.CENTER);
        labelVideo.setPreferredSize(new Dimension(LARGEUR_VIDEO, HAUTEUR_VIDEO));
        labelVideo.setBackground(Color.BLACK);
        labelVideo.setOpaque(true);

        String nom = estLocal ? client.getNomUtilisateur() + " (vous)" : interlocuteur;
        JLabel labelNom = new JLabel(nom, SwingConstants.CENTER);
        labelNom.setForeground(Color.WHITE);
        labelNom.setFont(new Font("Arial", Font.BOLD, 12));
        labelNom.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        if (estLocal) {
            labelVideoLocal = labelVideo;
            labelVideoLocal.setText("<html><center><font color='gray'>Caméra locale...</font></center></html>");
        } else {
            labelVideoDistant = labelVideo;
            labelVideoDistant.setText("<html><center><font color='gray'>En attente vidéo...</font></center></html>");
        }

        panneau.add(labelVideo, BorderLayout.CENTER);
        panneau.add(labelNom,   BorderLayout.SOUTH);
        return panneau;
    }

    private JPanel creerPanneauBoutons() {
        JPanel panneau = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panneau.setBackground(new Color(30, 30, 30));

        boutonRaccrocher = new JButton("Raccrocher");
        boutonRaccrocher.setBackground(new Color(220, 50, 50));
        boutonRaccrocher.setForeground(Color.WHITE);
        boutonRaccrocher.setFont(new Font("Arial", Font.BOLD, 14));
        boutonRaccrocher.setFocusPainted(false);
        boutonRaccrocher.setPreferredSize(new Dimension(160, 45));
        boutonRaccrocher.addActionListener(e -> raccrocher());

        boutonMute = new JButton("Muet");
        boutonMute.setBackground(new Color(80, 80, 80));
        boutonMute.setForeground(Color.WHITE);
        boutonMute.setFont(new Font("Arial", Font.BOLD, 14));
        boutonMute.setFocusPainted(false);
        boutonMute.setPreferredSize(new Dimension(140, 45));
        boutonMute.addActionListener(e -> basculerMute());

        panneau.add(boutonMute);
        panneau.add(boutonRaccrocher);
        return panneau;
    }

    // ■■ Méthodes privées Logique ■■■■■■■■■■■■■■■■■■■■

    private void demarrerMedia() {
        videoSender   = new VideoSender(client, interlocuteur);
        videoReceiver = new VideoReceiver(labelVideoDistant);
        audioSender   = new AudioSender(client, interlocuteur);
        audioReceiver = new AudioReceiver();
        videoSender.demarrer();
        videoReceiver.demarrer();
        audioSender.demarrer();
        audioReceiver.demarrer();
    }

    private void arreterMedia() {
        if (videoSender   != null) videoSender.arreter();
        if (videoReceiver != null) videoReceiver.arreter();
        if (audioSender   != null) audioSender.arreter();
        if (audioReceiver != null) audioReceiver.arreter();
    }

    private void raccrocher() {
        client.envoyer(new Message(
                client.getNomUtilisateur(), interlocuteur,
                "", TypeMessage.CALL_REJECT));
        terminerAppel();
    }

    private void basculerMute() {
        muet = !muet;
        if (muet) {
            audioSender.arreter();
            boutonMute.setText("Reactiver");
            boutonMute.setBackground(new Color(180, 100, 0));
        } else {
            audioSender.demarrer();
            boutonMute.setText("Muet");
            boutonMute.setBackground(new Color(80, 80, 80));
        }
    }

    // ■■ Getters ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    public VideoReceiver getVideoReceiver() { return videoReceiver; }
    public AudioReceiver getAudioReceiver() { return audioReceiver; }
}