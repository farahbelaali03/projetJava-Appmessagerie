package database;

import java.sql.*;

public class UserDAO {

    public boolean login(String username, String password) {
        String sql =
                "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement s =
                     DatabaseConnection.getConnection().prepareStatement(sql)) {
            s.setString(1, username);
            s.setString(2, password);
            return s.executeQuery().next();
        } catch (SQLException e) {
            System.out.println("Erreur login : " + e.getMessage());
            return false;
        }
    }

    public boolean register(String username, String password) {
        String sql =
                "INSERT INTO users (username, password) VALUES(?, ?)";
        try (PreparedStatement s =
                     DatabaseConnection.getConnection().prepareStatement(sql)) {
            s.setString(1, username);
            s.setString(2, password);
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur register : " + e.getMessage());
            return false;
        }
    }

    public void updateStatus(String username, boolean online) {
        String sql =
                "UPDATE users SET online=? WHERE username=?";
        try (PreparedStatement s =
                     DatabaseConnection.getConnection().prepareStatement(sql)) {
            s.setBoolean(1, online);
            s.setString(2, username);
            s.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur status : " + e.getMessage());
        }
    }
}