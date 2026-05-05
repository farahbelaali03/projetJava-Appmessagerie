package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

    /**
     * DAO pour lister les conversations d'un utilisateur.
     * @author Afnane
     */
    public class ConversationDAO {

        // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

        /**
         * Retourne la liste des utilisateurs avec qui "nomUtilisateur" a échangé.
         * Utilisée pour afficher les conversations dans l'interface.
         */
        public List<String> getConversations(String nomUtilisateur) {
            String sql = "SELECT DISTINCT "
                    + "  CASE WHEN expediteur=? THEN destinataire ELSE expediteur END AS contact "
                    + "FROM messages "
                    + "WHERE expediteur=? OR destinataire=? "
                    + "ORDER BY contact ASC";
            List<String> contacts = new ArrayList<>();
            try (PreparedStatement s =
                         DatabaseConnection.getConnection().prepareStatement(sql)) {
                s.setString(1, nomUtilisateur);
                s.setString(2, nomUtilisateur);
                s.setString(3, nomUtilisateur);
                ResultSet rs = s.executeQuery();
                while (rs.next()) {
                    contacts.add(rs.getString("contact"));
                }
            } catch (SQLException e) {
                System.out.println("Erreur getConversations : " + e.getMessage());
            }
            return contacts;
        }

        /**
         * Retourne le dernier message échangé entre deux utilisateurs.
         * Utile pour afficher un aperçu dans la liste de conversations.
         */
        public String getDernierMessage(String user1, String user2) {
            String sql = "SELECT contenu FROM messages "
                    + "WHERE (expediteur=? AND destinataire=?) "
                    + "   OR (expediteur=? AND destinataire=?) "
                    + "ORDER BY id DESC LIMIT 1";
            try (PreparedStatement s =
                         DatabaseConnection.getConnection().prepareStatement(sql)) {
                s.setString(1, user1);
                s.setString(2, user2);
                s.setString(3, user2);
                s.setString(4, user1);
                ResultSet rs = s.executeQuery();
                if (rs.next()) {
                    return rs.getString("contenu");
                }
            } catch (SQLException e) {
                System.out.println("Erreur getDernierMessage : " + e.getMessage());
            }
            return "";
        }
    }
