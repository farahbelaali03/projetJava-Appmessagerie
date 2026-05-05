package database;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL =
            "jdbc:mysql://localhost:3306/messagerie_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion MySQL réussie !");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur MySQL : " + e.getMessage());
        }
        return connection;
    }
}