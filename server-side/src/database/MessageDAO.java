package database;

import model.Message;
import model.TypeMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

    /**
     * DAO pour la gestion des messages en base de données.
     * Sauvegarde les messages et récupère l'historique.
     * @author Afnane
     */
    public class MessageDAO {

        // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

        /**
         * Sauvegarde un message texte ou fichier dans la base.
         * Appelée par ClientHandler.handleMessage() et handleFile().
         */
        public void sauvegarderMessage(Message msg) {
            String sql = "INSERT INTO messages (expediteur, destinataire, contenu, type) "
                    + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement s =
                         DatabaseConnection.getConnection().prepareStatement(sql)) {
                s.setString(1, msg.getExpediteur());
                s.setString(2, msg.getDestinataire());
                s.setString(3, msg.getContenu());
                s.setString(4, msg.getType().name());
                s.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Erreur sauvegarderMessage : " + e.getMessage());
            }
        }

        /**
         * Récupère l'historique entre deux utilisateurs (ordre chronologique).
         * Appelée par ChatController (côté client via le serveur).
         */
        public List<Message> getHistorique(String user1, String user2) {
            String sql = "SELECT expediteur, destinataire, contenu, type "
                    + "FROM messages "
                    + "WHERE (expediteur=? AND destinataire=?) "
                    + "   OR (expediteur=? AND destinataire=?) "
                    + "ORDER BY id ASC";
            List<Message> historique = new ArrayList<>();
            try (PreparedStatement s =
                         DatabaseConnection.getConnection().prepareStatement(sql)) {
                s.setString(1, user1);
                s.setString(2, user2);
                s.setString(3, user2);
                s.setString(4, user1);
                ResultSet rs = s.executeQuery();
                while (rs.next()) {
                    Message msg = new Message(
                            rs.getString("expediteur"),
                            rs.getString("destinataire"),
                            rs.getString("contenu"),
                            TypeMessage.valueOf(rs.getString("type"))
                    );
                    historique.add(msg);
                }
            } catch (SQLException e) {
                System.out.println("Erreur getHistorique : " + e.getMessage());
            }
            return historique;
        }

        // ■■ Méthodes privées ■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        // (rien pour l'instant)
    }
