package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des contacts utilisateurs.
 * @author Amal
 */
public class ContactDAO {

    // ════════════════════════════════
    // RÉCUPÉRER LES CONTACTS
    // ════════════════════════════════

    /**
     * Retourne la liste des contacts enregistrés pour un utilisateur.
     *
     * @param username  nom de l'utilisateur connecté
     * @return liste des noms de contacts (vide si aucun)
     */
    public List<String> getContacts(String username) {

        List<String> contacts = new ArrayList<>();
        String sql = "SELECT contact_username FROM contacts WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contacts.add(rs.getString("contact_username"));
            }

        } catch (Exception e) {
            System.out.println("Erreur getContacts : " + e.getMessage());
        }

        return contacts;
    }

    // ════════════════════════════════
    // AJOUTER UN CONTACT
    // ════════════════════════════════

    /**
     * Ajoute un contact dans la liste d'un utilisateur.
     * Relation bidirectionnelle : A ajoute B → B est aussi dans la liste de A.
     * (relation unidirectionnelle ici — à ajuster selon le besoin)
     *
     * @param username       utilisateur qui ajoute
     * @param nomContact     utilisateur à ajouter
     * @return true si succès, false sinon
     */
    public boolean ajouterContact(String username, String nomContact) {

        String sql = "INSERT IGNORE INTO contacts (username, contact_username) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, nomContact);
            int lignes = stmt.executeUpdate();
            return lignes > 0;

        } catch (Exception e) {
            System.out.println("Erreur ajouterContact : " + e.getMessage());
            return false;
        }
    }

    // ════════════════════════════════
    // SUPPRIMER UN CONTACT
    // ════════════════════════════════

    /**
     * Supprime un contact de la liste d'un utilisateur.
     *
     * @param username       utilisateur qui supprime
     * @param nomContact     contact à supprimer
     * @return true si succès, false sinon
     */
    public boolean supprimerContact(String username, String nomContact) {

        String sql = "DELETE FROM contacts WHERE username = ? AND contact_username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, nomContact);
            int lignes = stmt.executeUpdate();
            return lignes > 0;

        } catch (Exception e) {
            System.out.println("Erreur supprimerContact : " + e.getMessage());
            return false;
        }
    }
}
