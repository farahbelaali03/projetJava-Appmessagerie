package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * DAO pour la gestion des appels dans la base de données.
 * @author Farah
 */
public class CallDAO {

    // ■■ Constantes ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
    private static final String SQL_SAUVEGARDER =
            "INSERT INTO calls (appelant, recepteur, statut, date_appel) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACCEPTER =
            "UPDATE calls SET statut = 'ACCEPTE' WHERE appelant = ? AND recepteur = ? AND statut = 'EN_ATTENTE'";
    private static final String SQL_REFUSER =
            "UPDATE calls SET statut = 'REFUSE' WHERE appelant = ? AND recepteur = ? AND statut = 'EN_ATTENTE'";
    private static final String SQL_TERMINER =
            "UPDATE calls SET statut = 'TERMINE', date_fin = ? WHERE appelant = ? AND recepteur = ? AND statut = 'ACCEPTE'";

    // ■■ Méthodes publiques ■■■■■■■■■■■■■■■■■■■■■■■■■■■

    public void sauvegarderAppel(String appelant, String recepteur) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SAUVEGARDER)) {
            stmt.setString(1, appelant);
            stmt.setString(2, recepteur);
            stmt.setString(3, "EN_ATTENTE");
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur sauvegarderAppel : " + e.getMessage());
        }
    }

    public void accepterAppel(String appelant, String recepteur) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ACCEPTER)) {
            stmt.setString(1, appelant);
            stmt.setString(2, recepteur);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur accepterAppel : " + e.getMessage());
        }
    }

    public void refuserAppel(String appelant, String recepteur) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REFUSER)) {
            stmt.setString(1, appelant);
            stmt.setString(2, recepteur);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur refuserAppel : " + e.getMessage());
        }
    }

    public void terminerAppel(String appelant, String recepteur) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TERMINER)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, appelant);
            stmt.setString(3, recepteur);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur terminerAppel : " + e.getMessage());
        }
    }
}