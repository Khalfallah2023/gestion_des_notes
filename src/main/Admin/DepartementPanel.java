
package main.Admin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.toedter.calendar.JDateChooser;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.List;

public class DepartementPanel extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(246, 246, 249);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);
    private static final String DB_URL = "jdbc:mysql://localhost/ensit";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    
    private JPanel contentPanel;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;
    private String currentClassName;
   
    private void setupHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Gestion des Étudiants");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel actionPanel = createActionPanel();
        headerPanel.add(actionPanel, BorderLayout.EAST);
        
        contentPanel.add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(BACKGROUND_COLOR);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchButton.setBackground(ACCENT_COLOR);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setContentAreaFilled(true);
        searchButton.setOpaque(true);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(BorderFactory.createCompoundBorder(
            searchButton.getBorder(), 
            new EmptyBorder(8, 15, 8, 15)
        ));
        searchButton.addActionListener(e -> filterStudents());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        JButton addButton = new JButton("Ajouter");
        JButton editButton = new JButton("Modifier");
        JButton deleteButton = new JButton("Supprimer");
        JButton printButton = new JButton("Imprimer");
        addButton.setBackground(ACCENT_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setContentAreaFilled(true);
        addButton.setOpaque(true);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setBorder(BorderFactory.createCompoundBorder(
            addButton.getBorder(), 
            new EmptyBorder(8, 15, 8, 15)
        ));
        editButton.setBackground(ACCENT_COLOR);
        editButton.setForeground(Color.WHITE);
        editButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setContentAreaFilled(true);
        editButton.setOpaque(true);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.setBorder(BorderFactory.createCompoundBorder(
            editButton.getBorder(), 
            new EmptyBorder(8, 15, 8, 15)
        ));
        addButton.addActionListener(e -> showAddStudentForm());
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un étudiant à modifier.");
                return;
            }
            showEditStudentForm(selectedRow);
        });
        deleteButton.setBackground(ACCENT_COLOR);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setOpaque(true);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
            deleteButton.getBorder(), 
            new EmptyBorder(8, 15, 8, 15)
        ));
        deleteButton.addActionListener(e -> deleteStudent());
        printButton.setBackground(ACCENT_COLOR);
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        printButton.setBorderPainted(false);
        printButton.setFocusPainted(false);
        printButton.setContentAreaFilled(true);
        printButton.setOpaque(true);
        printButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printButton.setBorder(BorderFactory.createCompoundBorder(
            printButton.getBorder(), 
            new EmptyBorder(8, 15, 8, 15)
        ));
        printButton.addActionListener(e -> printTable());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(printButton);
        actionPanel.add(searchPanel, BorderLayout.WEST);
        actionPanel.add(buttonPanel, BorderLayout.EAST);
        
        return actionPanel;
    }

    private void setupTable() {
        // Configuration du modèle
        String[] columns = {"ID", "Nom", "Prénom", "Date de naissance", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Configuration de la table
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Définir les largeurs des colonnes
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Nom
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Prénom
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Date
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Email
        
        // Créer le scroll pane pour la table
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Définir une taille préférée pour le scroll pane
        tableScrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Créer un panel pour contenir la table avec des marges
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        tableContainer.add(tableScrollPane, BorderLayout.CENTER);
        
        // Ajouter le conteneur de table au contentPanel
        contentPanel.add(tableContainer, BorderLayout.CENTER);
        
        System.out.println("Table setup complete - Dimensions: " + tableScrollPane.getPreferredSize());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Forcer la mise à jour des dimensions après l'ajout au conteneur parent
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
    // Modification du constructeur
    public DepartementPanel() {
        // Utiliser un BorderLayout avec des marges
        setLayout(new BorderLayout(10, 10));
        
        // Initialiser le contentPanel
        contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Initialiser les composants
        setupHeader();
        setupTable();
        
        // Ajouter le contentPanel dans un JScrollPane
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Ajouter le scrollPane au panel principal
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Définir une taille minimum
        setPreferredSize(new Dimension(900, 600));
        setMinimumSize(new Dimension(800, 500));
        
        System.out.println("DepartementPanel initialization complete");
    }
    

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && currentClassName != null) {
            // Recharger les données quand le panel devient visible
            loadStudentData();
        }
    }
        private void loadStudentData() {
            System.out.println("Entrée dans loadStudentData");
            
            if (currentClassName == null || currentClassName.trim().isEmpty()) {
                System.out.println("Pas de classe sélectionnée");
                return;
            }
            
            System.out.println("Début du chargement des données...");
            
            new SwingWorker<List<Object[]>, Void>() {
                @Override
                protected List<Object[]> doInBackground() throws Exception {
                    System.out.println("SwingWorker: début du chargement");
                    List<Object[]> rows = new ArrayList<>();
                    
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        System.out.println("Connexion à la base de données établie");
                        
                        String sql = """
                            SELECT e.ID_étudiant, e.Nom, e.Prénom, e.Date_naissance, e.Email 
                            FROM Étudiant e
                            INNER JOIN classe c ON e.ID_classe = c.ID_classe 
                            WHERE c.Nom_classe = ?
                        """;
                        
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, currentClassName);
                            System.out.println("Exécution de la requête pour: " + currentClassName);
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                    Object[] row = {
                                        rs.getInt("ID_étudiant"),
                                        rs.getString("Nom"),
                                        rs.getString("Prénom"),
                                        rs.getString("Date_naissance"),
                                        rs.getString("Email")
                                    };
                                    rows.add(row);
                                }
                            }
                        }
                    }
                    
                    System.out.println("Nombre d'enregistrements trouvés: " + rows.size());
                    return rows;
                }
    
                @Override
                protected void done() {
                    try {
                        List<Object[]> rows = get();
                        SwingUtilities.invokeLater(() -> {
                            tableModel.setRowCount(0);
                            for (Object[] row : rows) {
                                tableModel.addRow(row);
                            }
                            
                            // Forcer la mise à jour des dimensions
                            table.setPreferredScrollableViewportSize(
                                new Dimension(Math.max(800, table.getPreferredSize().width),
                                            Math.max(400, table.getPreferredSize().height)));
                            
                            // Forcer le rafraîchissement complet
                            table.revalidate();
                            table.repaint();
                            contentPanel.revalidate();
                            contentPanel.repaint();
                            revalidate();
                            repaint();
                            
                            // Debug des dimensions
                            System.out.println("Dimensions après mise à jour:");
                            System.out.println("Table: " + table.getSize());
                            System.out.println("ScrollPane: " + table.getParent().getParent().getSize());
                            System.out.println("ContentPanel: " + contentPanel.getSize());
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }}
                }.execute(); 
            
        }
            
    
        // Modifiez aussi la méthode setCurrentClass pour mieux tracer l'exécution
        public void setCurrentClass(String className) {
            System.out.println("setCurrentClass appelé avec: " + className);
            
            if (className == null || className.trim().isEmpty()) {
                System.out.println("Nom de classe invalide");
                return;
            }
    
            // Mettre à jour le nom de la classe
            this.currentClassName = className;
            System.out.println("currentClassName mis à jour: " + this.currentClassName);
    
            // S'assurer que le tableau est initialisé avant de charger les données
            if (tableModel == null) {
                System.out.println("ERREUR: TableModel non initialisé!");
                setupTable();
            }
    
            // Vider le tableau existant
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                System.out.println("Tableau vidé");
                
                // Mettre à jour le titre
                updateTitle();
                System.out.println("Titre mis à jour");
                
                // Charger les données
                System.out.println("Démarrage du chargement des données");
                loadStudentData();
            });
        }
        // Vérifiez aussi la configuration de votre base de données
        static {
            try {
                // Charger explicitement le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("Driver MySQL chargé avec succès");
            } catch (ClassNotFoundException e) {
                System.err.println("Erreur de chargement du driver MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private void updateTitle() {
            if (currentClassName != null) {
                System.out.println("Mise à jour du titre pour: " + currentClassName);
                SwingUtilities.invokeLater(() -> {
                    // Rechercher le JLabel du titre dans le headerPanel
                    if (contentPanel != null) {
                        Component[] components = contentPanel.getComponents();
                        for (Component component : components) {
                            if (component instanceof JPanel headerPanel) {
                                Component[] headerComponents = headerPanel.getComponents();
                                for (Component headerComp : headerComponents) {
                                    if (headerComp instanceof JLabel titleLabel) {
                                        titleLabel.setText("Gestion des Étudiants - " + currentClassName);
                                        headerPanel.revalidate();
                                        headerPanel.repaint();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    private void filterStudents() {
        String searchText = searchField.getText();
        tableModel.setRowCount(0);
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String sql = "SELECT ID_étudiant, Nom, Prénom, Date_naissance, Email FROM Étudiant WHERE ID_classe = (SELECT ID_classe FROM Classe WHERE Nom_classe = ?) AND (ID_étudiant LIKE ? OR Nom LIKE ? OR Prénom LIKE ? OR Email LIKE ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, currentClassName);
            statement.setString(2, "%" + searchText + "%");
            statement.setString(3, "%" + searchText + "%");
            statement.setString(4, "%" + searchText + "%");
            statement.setString(5, "%" + searchText + "%");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ID_étudiant");
                String nom = resultSet.getString("Nom");
                String prenom = resultSet.getString("Prénom");
                String dateNaissance = resultSet.getString("Date_naissance");
                String email = resultSet.getString("Email");
                tableModel.addRow(new Object[]{id, nom, prenom, dateNaissance, email});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAddStudentForm() {
        JFrame addStudentFrame = new JFrame("Ajouter un élève");
        addStudentFrame.setSize(400, 300);
        addStudentFrame.setLocationRelativeTo(this);
    
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font font = new Font("Arial", Font.PLAIN, 14);
    
        // Créer les champs avec des références
        JTextField nomField = new JTextField();
        JTextField prenomField = new JTextField();
        JTextField emailField = new JTextField();
        JDateChooser dateChooser = new JDateChooser();
        
        // Ajouter les champs au panel avec leurs labels
        addFormField(panel, gbc, "Nom:", nomField, 0, font);
        addFormField(panel, gbc, "Prénom:", prenomField, 1, font);
        
        // Ajouter le JDateChooser
        JLabel dateLabel = new JLabel("Date de naissance:");
        dateLabel.setFont(font);
        gbc.gridx = 0; 
        gbc.gridy = 2;
        panel.add(dateLabel, gbc);
        gbc.gridx = 1;
        panel.add(dateChooser, gbc);
    
        // Ajouter le champ email
        addFormField(panel, gbc, "Email:", emailField, 3, font);
    
        // Boutons
        gbc.gridx = 0; 
        gbc.gridy = 4;
        JButton backButton = new JButton("Retour");
        backButton.setFont(font);
        backButton.addActionListener(e -> addStudentFrame.dispose());
        panel.add(backButton, gbc);
    
        gbc.gridx = 1;
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.setFont(font);
        saveButton.addActionListener(e -> {
            if (!isValidEmail(emailField.getText())) {
                JOptionPane.showMessageDialog(addStudentFrame, "Erreur : Email invalide.");
                return;
            }
    
            java.util.Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(addStudentFrame, "Erreur : Date de naissance invalide.");
                return;
            }
    
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
            try {
                addStudent(nomField.getText(), prenomField.getText(), sqlDate.toString(), emailField.getText());
                addStudentFrame.dispose();
                loadStudentData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(addStudentFrame, "Erreur lors de l'ajout : " + ex.getMessage());
            }
        });
        panel.add(saveButton, gbc);
    
        addStudentFrame.add(panel);
        addStudentFrame.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        field.setFont(font);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    public void showEditStudentForm(int row) {
        JFrame editStudentFrame = new JFrame("Modifier un élève");
        editStudentFrame.setSize(400, 300);
        editStudentFrame.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font font = new Font("Arial", Font.PLAIN, 14);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nomLabel = new JLabel("Nom:");
        nomLabel.setFont(font);
        panel.add(nomLabel, gbc);

        gbc.gridx = 1;
        JTextField nomField = new JTextField(table.getValueAt(row, 1).toString());
        nomField.setFont(font);
        panel.add(nomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel prenomLabel = new JLabel("Prénom:");
        prenomLabel.setFont(font);
        panel.add(prenomLabel, gbc);

        gbc.gridx = 1;
        JTextField prenomField = new JTextField(table.getValueAt(row, 2).toString());
        prenomField.setFont(font);
        panel.add(prenomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel dateNaissanceLabel = new JLabel("Date de naissance:");
        dateNaissanceLabel.setFont(font);
        panel.add(dateNaissanceLabel, gbc);

        gbc.gridx = 1;
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(150, 25)); // Ajuster la taille du JDateChooser
        // Définir la date initiale du JDateChooser
        String dateNaissanceStr = table.getValueAt(row, 3).toString();
        try {
            Date dateNaissance = new SimpleDateFormat("yyyy-MM-dd").parse(dateNaissanceStr);
            dateChooser.setDate(dateNaissance);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        panel.add(dateChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(font);
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(table.getValueAt(row, 4).toString());
        emailField.setFont(font);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton backButton = new JButton("Retour");
        backButton.setFont(font);
        backButton.addActionListener(e -> editStudentFrame.dispose());
        panel.add(backButton, gbc);

        gbc.gridx = 1;
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.setFont(font);
        saveButton.addActionListener(e -> {
            if (!isValidEmail(emailField.getText())) {
                JOptionPane.showMessageDialog(editStudentFrame, "Erreur : Email invalide.");
                return;
            }
            java.util.Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(editStudentFrame, "Erreur : Date de naissance invalide.");
                return;
            }
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
            updateStudent((int) table.getValueAt(row, 0), nomField.getText(), prenomField.getText(), sqlDate.toString(), emailField.getText());
            editStudentFrame.dispose();
            loadStudentData(); // Rafraîchir les données du tableau après la mise à jour
        });
        panel.add(saveButton, gbc);

        editStudentFrame.add(panel);
        editStudentFrame.setVisible(true);
    }

    private void updateStudent(int id, String nom, String prenom, String dateNaissance, String email) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String sql = "UPDATE Étudiant SET Nom = ?, Prénom = ?, Date_naissance = ?, Email = ? WHERE ID_étudiant = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nom);
            statement.setString(2, prenom);
            statement.setString(3, dateNaissance);
            statement.setString(4, email);
            statement.setInt(5, id);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Rafraîchir les données du tableau après la mise à jour
        loadStudentData();
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un étudiant à supprimer.");
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir supprimer cet étudiant ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION) {
            return; // Annuler la suppression si l'utilisateur choisit "Non"
        }
        int id = (int) table.getValueAt(selectedRow, 0);
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String sql = "DELETE FROM Étudiant WHERE ID_étudiant = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Rafraîchir les données du tableau après la suppression
        loadStudentData();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void addStudent(String nom, String prenom, String dateNaissance, String email) {
        if (currentClassName == null) {
            JOptionPane.showMessageDialog(this, "Erreur : Aucune classe sélectionnée.");
            return;
        }
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            // Récupérer l'ID de la classe depuis la base de données
            String getClassIdSql = "SELECT ID_classe FROM classe WHERE Nom_classe = ?";
            PreparedStatement classStmt = connection.prepareStatement(getClassIdSql);
            classStmt.setString(1, currentClassName);
            ResultSet classRs = classStmt.executeQuery();
            if (!classRs.next()) {
                JOptionPane.showMessageDialog(this, "Erreur : Classe non trouvée.");
                return;
            }
            int classId = classRs.getInt("ID_classe");
            // Insérer l'étudiant
            String insertSql = "INSERT INTO Étudiant (Nom, Prénom, Date_naissance, Email, ID_classe) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertSql);
            insertStmt.setString(1, nom);
            insertStmt.setString(2, prenom);
            insertStmt.setString(3, dateNaissance);
            insertStmt.setString(4, email);
            insertStmt.setInt(5, classId);

            insertStmt.executeUpdate();

            loadStudentData();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(this, "Erreur : Cet email est déjà utilisé.");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : " + e.getMessage());
            }
            e.printStackTrace();
        }
        loadStudentData();
    }

    private void printTable() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(table.getPrintable(JTable.PrintMode.FIT_WIDTH, new MessageFormat("Liste des étudiants"), null));
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'impression : " + e.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gestion des Départements");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            DepartementPanel panel = new DepartementPanel();
            frame.add(panel);
            
            // Définir une taille minimum pour la fenêtre
            frame.setMinimumSize(new Dimension(1000, 700));
            
            // Maximiser la fenêtre
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            
            // Centrer et afficher
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        
            
            // Debug : imprimer les dimensions
            System.out.println("Dimensions de la fenêtre : " + frame.getSize());
            System.out.println("Dimensions du panel : " + panel.getSize());
        });
    }
    
}