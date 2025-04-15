package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String URL = "jdbc:mysql://localhost/ensit?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public ConnectionManager() {
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Chargez uniquement le nouveau driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données réussie !");
            
        } catch (ClassNotFoundException e) {
            System.out.println("Pilote MySQL JDBC non trouvé.");
            System.out.println("Vérifiez que le fichier mysql-connector-java-8.0.33.jar est bien dans le dossier lib/");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Échec de la connexion ! Vérifiez la console");
            System.out.println("URL: " + URL);
            System.out.println("Utilisateur: " + USER);
            e.printStackTrace();
        }
        
        return connection;
    }
    
    public static void main(String[] args) {
        getConnection();
    }
}