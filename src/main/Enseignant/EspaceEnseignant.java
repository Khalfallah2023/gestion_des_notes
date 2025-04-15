package main.Enseignant;

import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.FloatStringConverter;
import javafx.scene.effect.DropShadow;

public class EspaceEnseignant extends BorderPane {
    private int idEnseignant;
    private ListView<String> classList;
    private TableView<StudentNote> notesTable;
  
    private String currentNoteType;
    Button dsButton = new Button("Note DS");
    Button tpButton = new Button("Note TP");
    Button examButton = new Button("Note Examen");
      public int idMatiere;
      
    public EspaceEnseignant(int idEnseignant) {
        this.idEnseignant = idEnseignant;
        this.idMatiere = getMatiereId();
        initialize();
    }

    private void initialize() {
        setTop(createHeader());
        
        // Créer un conteneur principal avec padding
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f5f5f7;");
        
        // Section gauche (classes)
        VBox classesSection = createClassesSection();
        
        // Section droite (notes)
        VBox notesSection = createNotesSection();
        
        mainContent.getChildren().addAll(classesSection, notesSection);
        setCenter(mainContent);
        
        // Configuration des boutons
        configureNoteButtons();
    }
    private VBox createNotesSection() {
        VBox notesSection = new VBox(20);
        notesSection.setStyle("-fx-background-color: white; -fx-padding: 20px; " +
                            "-fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        HBox.setHgrow(notesSection, Priority.ALWAYS);

        // Titre
        Label titleLabel = new Label("Gestion des Notes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Boutons de type de notes dans une HBox avec espacement égal
        HBox noteTypeButtons = createNoteTypeButtons();
        
        // Table des notes
        notesTable = createNotesTable();
        
        // Boutons d'action
        HBox actionButtons = createActionButtons();

        notesSection.getChildren().addAll(titleLabel, noteTypeButtons, notesTable, actionButtons);
        return notesSection;
    }
    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db); -fx-padding: 15px;");
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setEffect(new DropShadow());

        Label title = new Label("Espace Enseignant");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-font-size: 14px; -fx-padding: 8px 15px; " +
                            "-fx-background-radius: 5px; -fx-cursor: hand;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(title, spacer, logoutButton);
        return header;
    }
    private VBox createClassesSection() {
        VBox classesSection = new VBox(15);
        classesSection.setPrefWidth(300);
        classesSection.setStyle("-fx-background-color: white; -fx-padding: 20px; " +
                              "-fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label titleLabel = new Label("Mes Classes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        classList = new ListView<>();
        classList.setStyle("-fx-background-radius: 5px; -fx-border-radius: 5px; " +
                         "-fx-border-color: #e0e0e0; -fx-border-width: 1px;");
        
        loadClassList();
        
        classesSection.getChildren().addAll(titleLabel, classList);
        return classesSection;
    }
    private void loadClassList() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String sql = "SELECT DISTINCT c.Nom_classe " +
                        "FROM classe c " +
                        "INNER JOIN dispense d ON c.ID_classe = d.ID_classe " +
                        "WHERE d.ID_enseignant = ?";
                        
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idEnseignant);
            
            System.out.println("Executing query for teacher ID: " + idEnseignant); // Debug
            ResultSet rs = stmt.executeQuery();
            ObservableList<String> classes = FXCollections.<String>observableArrayList();
            
            while (rs.next()) {
                String className = rs.getString("Nom_classe");
                classes.add(className);
                System.out.println("Found class: " + className); // Debug
            }
            
            classList.setItems(classes);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de base de données: " + e.getMessage());
        }
    }
   
    
    private HBox createNoteTypeButtons() {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Style commun pour les boutons
        String buttonStyle = "-fx-font-size: 14px; -fx-padding: 10px 20px; " +
                           "-fx-background-radius: 5px; -fx-cursor: hand; " +
                           "-fx-font-weight: bold; -fx-text-fill: white;";

        dsButton.setStyle(buttonStyle + "-fx-background-color: #3498db;");
        tpButton.setStyle(buttonStyle + "-fx-background-color: #2ecc71;");
        examButton.setStyle(buttonStyle + "-fx-background-color: #e74c3c;");

        // Effet hover
        dsButton.setOnMouseEntered(e -> dsButton.setStyle(buttonStyle + "-fx-background-color: #2980b9;"));
        dsButton.setOnMouseExited(e -> dsButton.setStyle(buttonStyle + "-fx-background-color: #3498db;"));

        tpButton.setOnMouseEntered(e -> tpButton.setStyle(buttonStyle + "-fx-background-color: #27ae60;"));
        tpButton.setOnMouseExited(e -> tpButton.setStyle(buttonStyle + "-fx-background-color: #2ecc71;"));

        examButton.setOnMouseEntered(e -> examButton.setStyle(buttonStyle + "-fx-background-color: #c0392b;"));
        examButton.setOnMouseExited(e -> examButton.setStyle(buttonStyle + "-fx-background-color: #e74c3c;"));

        buttonBox.getChildren().addAll(dsButton, tpButton, examButton);
        return buttonBox;
    }
    
    private TableView<StudentNote> createNotesTable() {
        TableView<StudentNote> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                      "-fx-border-width: 1px; -fx-border-radius: 5px; " +
                      "-fx-background-radius: 5px;");
        
        // Configuration des colonnes
        TableColumn<StudentNote, String> idCol = new TableColumn<>("ID");
        TableColumn<StudentNote, String> nomCol = new TableColumn<>("Nom");
        TableColumn<StudentNote, String> prenomCol = new TableColumn<>("Prénom");
        TableColumn<StudentNote, Float> noteCol = new TableColumn<>("Note");

        // Style des colonnes
        String columnStyle = "-fx-alignment: CENTER-LEFT; -fx-padding: 10px;";
        idCol.setStyle(columnStyle);
        nomCol.setStyle(columnStyle);
        prenomCol.setStyle(columnStyle);
        noteCol.setStyle(columnStyle);

        // Configuration des cellules
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        
        // Edition des notes
        noteCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        noteCol.setOnEditCommit(event -> {
            StudentNote studentNote = event.getRowValue();
            studentNote.setNote(event.getNewValue());
        });

        // Largeur des colonnes
        idCol.setPrefWidth(100);
        nomCol.setPrefWidth(150);
        prenomCol.setPrefWidth(150);
        noteCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nomCol, prenomCol, noteCol);
        table.setEditable(true);
        return table;
    }

   
    private HBox createActionButtons() {
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));
    
        Button saveButton = new Button("Sauvegarder");
        Button printButton = new Button("Imprimer");
    
        String buttonStyle = "-fx-font-size: 14px; -fx-padding: 10px 20px; " +
                           "-fx-background-radius: 5px; -fx-cursor: hand; " +
                           "-fx-font-weight: bold; -fx-text-fill: white;";
    
        saveButton.setStyle(buttonStyle + "-fx-background-color: #2ecc71;");
        printButton.setStyle(buttonStyle + "-fx-background-color: #95a5a6;");
    
        // Hover effects
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(buttonStyle + "-fx-background-color: #27ae60;"));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(buttonStyle + "-fx-background-color: #2ecc71;"));
    
        printButton.setOnMouseEntered(e -> printButton.setStyle(buttonStyle + "-fx-background-color: #7f8c8d;"));
        printButton.setOnMouseExited(e -> printButton.setStyle(buttonStyle + "-fx-background-color: #95a5a6;"));
    
        // Configuration des actions
        saveButton.setOnAction(e -> {
            if (currentNoteType == null) {
                showError("Veuillez sélectionner un type de note (DS, TP ou Examen)");
                return;
            }
            if (notesTable.getItems().isEmpty()) {
                showError("Aucune note à sauvegarder");
                return;
            }
            saveNotes(idMatiere);
        });
    
        printButton.setOnAction(e -> {
            if (notesTable.getItems().isEmpty()) {
                showError("Aucune note à imprimer");
                return;
            }
            printNotes();
        });
    
        actionButtons.getChildren().addAll(saveButton, printButton);
        return actionButtons;
    }

    private void configureNoteButtons() {
        dsButton.setOnAction(e -> loadNotes("DS", true));
        tpButton.setOnAction(e -> loadNotes("TP", true));
        examButton.setOnAction(e -> loadNotes("Examen", false));
    }

    private int getMatiereId() {
        int idMatiere = -1;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String query = "SELECT ID_matière FROM enseigne WHERE ID_enseignant = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, idEnseignant);
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                idMatiere = resultSet.getInt("ID_matière");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération de l'ID matière: " + e.getMessage());
        }
        return idMatiere;
    }
   
        private void loadNotes(String type, boolean includeNames) {
            String selectedClass = classList.getSelectionModel().getSelectedItem();
            if (selectedClass == null) {
                showError("Veuillez sélectionner une classe!");
                return;
            }
        
            currentNoteType = type;
            
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
                String query = includeNames ?
                    "SELECT e.ID_étudiant AS Identifiant, e.Nom, e.Prénom, n.note " +
                    "FROM étudiant e " +
                    "JOIN classe c ON e.ID_classe = c.ID_classe " +
                    "JOIN dispense d ON c.ID_classe = d.ID_classe " +
                    "LEFT JOIN (SELECT ID_étudiant, note FROM notes WHERE type = ? AND ID_matière = (SELECT ID_matière FROM enseigne WHERE ID_enseignant = ?)) n " +
                    "ON e.ID_étudiant = n.ID_étudiant " +
                    "WHERE d.ID_enseignant = ? AND c.Nom_classe = ?" :
                    "SELECT e.ID_étudiant AS Identifiant, n.note " +
                    "FROM étudiant e " +
                    "JOIN classe c ON e.ID_classe = c.ID_classe " +
                    "JOIN dispense d ON c.ID_classe = d.ID_classe " +
                    "LEFT JOIN (SELECT ID_étudiant, note FROM notes WHERE type = ? AND ID_matière = (SELECT ID_matière FROM enseigne WHERE ID_enseignant = ?)) n " +
                    "ON e.ID_étudiant = n.ID_étudiant " +
                    "WHERE d.ID_enseignant = ? AND c.Nom_classe = ?";
        
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, type);
                stmt.setInt(2, idEnseignant);
                stmt.setInt(3, idEnseignant);
                stmt.setString(4, selectedClass);
                
                ResultSet rs = stmt.executeQuery();
                ObservableList<StudentNote> notes = FXCollections.observableArrayList();
                
                while (rs.next()) {
                    notes.add(new StudentNote(
                        rs.getString("Identifiant"),
                        includeNames ? rs.getString("Nom") : "",
                        includeNames ? rs.getString("Prénom") : "",
                        rs.getObject("note") != null ? rs.getFloat("note") : 0.0f
                    ));
                }
                
                notesTable.setItems(notes);
                notesTable.getColumns().forEach(col -> {
                    if (!includeNames && (col.getText().equals("Nom") || col.getText().equals("Prénom"))) {
                        col.setVisible(false);
                    } else {
                        col.setVisible(true);
                    }
                });
        
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur de chargement des notes: " + e.getMessage());
            }
        }
        private void saveNotes(int idMatiere) {
            String selectedClass = classList.getSelectionModel().getSelectedItem();
            if (selectedClass == null || currentNoteType == null) {
                showError("Veuillez sélectionner une classe et un type de note");
                return;
            }
        
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
                // Préparation de la requête d'insertion/mise à jour
                String saveQuery = "INSERT INTO notes (ID_étudiant, type, classe, note, ID_matière) " +
                                 "VALUES (?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE note = VALUES(note)";
                
                PreparedStatement saveStmt = conn.prepareStatement(saveQuery);
                
                for (StudentNote note : notesTable.getItems()) {
                    saveStmt.setString(1, note.getId());           // ID_etudiant
                    saveStmt.setString(2, currentNoteType);        // type (DS, TP, ou Examen)
                    saveStmt.setString(3, selectedClass);          // classe
                    saveStmt.setFloat(4, note.getNote());          // note
                    saveStmt.setInt(5, idMatiere);                // ID_matiere
                    saveStmt.addBatch();
                }
                
                saveStmt.executeBatch();
                showInfo("Notes sauvegardées avec succès!");
                
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    private void printNotes() {
    try {
        // Création d'une nouvelle fenêtre pour l'aperçu avant impression
        Stage printStage = new Stage();
        printStage.setTitle("Aperçu avant impression");

        // Création du contenu à imprimer
        VBox printContent = new VBox(10);
        printContent.setPadding(new Insets(20));

        // En-tête
        Label headerLabel = new Label("Notes de " + currentNoteType);
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label classLabel = new Label("Classe: " + classList.getSelectionModel().getSelectedItem());
        classLabel.setStyle("-fx-font-size: 14px;");

        // Table des notes pour l'impression
        TableView<StudentNote> printTable = new TableView<>(notesTable.getItems());
        printTable.getColumns().addAll(notesTable.getColumns());
        printTable.setEditable(false);

        // Bouton d'impression
        Button confirmPrintButton = new Button("Imprimer");
        confirmPrintButton.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                boolean success = job.printPage(printContent);
                if (success) {
                    job.endJob();
                    printStage.close();
                }
            }
        });

        printContent.getChildren().addAll(headerLabel, classLabel, printTable, confirmPrintButton);

        Scene printScene = new Scene(printContent);
        printStage.setScene(printScene);
        printStage.show();

    } catch (Exception e) {
        showError("Erreur lors de l'impression: " + e.getMessage());
    }
}
    private void printTableColumns(Connection conn) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getColumns(null, null, "dispense", null);
        System.out.println("Columns in dispense table:");
        while (rs.next()) {
            System.out.println(rs.getString("COLUMN_NAME"));
        }
    }
    

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe pour représenter une note d'étudiant
    public static class StudentNote {
        private String id;
        private String nom;
        private String prenom;
        private float note;

        public StudentNote(String id, String nom, String prenom, float note) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.note = note;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public float getNote() { return note; }
        public void setNote(float note) { this.note = note; }
    }
}