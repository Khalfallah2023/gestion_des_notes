package main.Admin;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import java.sql.*;
import javax.swing.SwingUtilities;
import main.Enseignant.EspaceEnseignant;

import main.Admin.MainApplication;


public class LoginPage extends Application {
    private Stage primaryStage;
    private TextField emailField;
    private PasswordField passwordField;
    static {
        try {
            // Chargement explicite du driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur de chargement du driver MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Connection getConnection() throws SQLException {
        try {
            // Ajout de paramètres de connexion pour éviter les problèmes de timezone
            String url = "jdbc:mysql://localhost/ensit?serverTimezone=UTC&useSSL=false";
            return DriverManager.getConnection(url, "root", "root");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        //new JFXPanel();
        this.primaryStage = primaryStage;
        VBox mainPane = createLoginForm();
        Scene scene = new Scene(mainPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    private VBox createLoginForm() {
        VBox mainPane = new VBox(10);
        mainPane.setAlignment(Pos.CENTER);
        mainPane.setPadding(new Insets(30));
        mainPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f0f2ff, #fafaff);");

        Label[] labels = createLabels();
        VBox formBox = createFormFields();
        
        mainPane.getChildren().addAll(labels);
        mainPane.getChildren().add(formBox);
        
        return mainPane;
    }

    private Label[] createLabels() {
        Label logoLabel = new Label("ENSIT");
        Label titleLabel = new Label("Welcome Back!");
        Label subtitleLabel = new Label("Sign in to continue to gestionnaire.");

        logoLabel.setFont(javafx.scene.text.Font.font("Arial", 32));
        titleLabel.setFont(javafx.scene.text.Font.font("Arial", 24));
        subtitleLabel.setFont(javafx.scene.text.Font.font("Arial", 14));

        logoLabel.setTextFill(javafx.scene.paint.Color.web("#748fe7"));
        titleLabel.setTextFill(javafx.scene.paint.Color.web("#748fe7"));
        subtitleLabel.setTextFill(javafx.scene.paint.Color.GRAY);

        return new Label[]{logoLabel, titleLabel, subtitleLabel};
    }

    private VBox createFormFields() {
        emailField = new TextField();
        passwordField = new PasswordField();
        Button loginButton = new Button("Log In");

        emailField.setPromptText("Enter your email");
        passwordField.setPromptText("Enter your password");
        styleFields(emailField, passwordField);
        styleLoginButton(loginButton);

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.getChildren().addAll(emailField, passwordField, loginButton);
        return formBox;
    }

    private void styleFields(TextField... fields) {
        for (TextField field : fields) {
            field.setMaxWidth(300);
            field.setFont(javafx.scene.text.Font.font("Arial", 14));
            field.setStyle("-fx-border-color: #c8c8c8; -fx-border-radius: 5px; -fx-padding: 10;");
        }
    }

    private void styleLoginButton(Button button) {
        button.setFont(javafx.scene.text.Font.font("Arial", 16));
        button.setStyle("-fx-background-color: #748fe7; -fx-text-fill: white; -fx-padding: 10px 20px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #607cd3; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #748fe7; -fx-text-fill: white;"));
        button.setOnAction(e -> authenticateUser());
    }

    private void authenticateUser() {
        System.out.println("Tentative d'authentification...");
        Connection conn = null;
        try {
            conn = getConnection();
            System.out.println("Connexion à la base de données réussie");
            
            if (checkAdmin(conn)) {
                System.out.println("Administrateur authentifié");
                openAdminInterface();
                return;
            }
            if (checkTeacher(conn)) {
                System.out.println("Enseignant authentifié");
                openTeacherInterface(getTeacherId(conn));
                return;
            }
            
            System.out.println("Identifiants invalides");
            showError("Identifiants invalides");
            
        } catch (SQLException ex) {
            System.err.println("Erreur SQL: " + ex.getMessage());
            showError("Erreur de connexion à la base de données: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Connexion fermée ?????,");
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                }
            }
        }
    }


    
    private boolean checkTeacher(Connection conn) throws SQLException {
        String sql = "SELECT ID_enseignant FROM enseignant WHERE Email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());
            return stmt.executeQuery().next();
        }
    }
    
    private int getTeacherId(Connection conn) throws SQLException {
        String sql = "SELECT ID_enseignant FROM enseignant WHERE Email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailField.getText());
            stmt.setString(2, passwordField.getText());
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt("ID_enseignant");
        }
    }

    private boolean checkAdmin(Connection conn) throws SQLException {
        String sql = "SELECT * FROM admin WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailField.getText().trim());
            stmt.setString(2, passwordField.getText());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    
    private void openAdminInterface() {
        try {
            // Fermer la fenêtre JavaFX actuelle
            Platform.runLater(() -> primaryStage.close());

            // Créer et afficher l'interface admin Swing sur l'EDT de Swing
            SwingUtilities.invokeLater(() -> {
                MainApplication mainApp = new MainApplication();
                mainApp.setVisible(true);
            });

        } catch (Exception e) {
            showError("Erreur d'ouverture: " + e.getMessage());
        }
    }



    private void openTeacherInterface(int idEnseignant) {
        Stage teacherStage = new Stage();
        teacherStage.setTitle("Espace Enseignant");
        Scene scene = new Scene(new EspaceEnseignant(idEnseignant), 1000, 600);
        teacherStage.setScene(scene);
        teacherStage.show();
        primaryStage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        if (!Platform.isFxApplicationThread()) {
            launch(args);
        }
    }
}
