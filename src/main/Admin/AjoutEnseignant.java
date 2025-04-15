package main.Admin;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AjoutEnseignant extends JPanel {
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField specialiteField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private ListeEnseignants parentPanel;

    public AjoutEnseignant(ListeEnseignants parent) {
        this.parentPanel = parent;
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Panel du titre
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Ajouter un Enseignant");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Panel du formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Création des champs
        nomField = createStyledTextField();
        prenomField = createStyledTextField();
        specialiteField = createStyledTextField();
        emailField = createStyledTextField();
        passwordField = createStyledPasswordField();

        // Ajout des composants avec GridBagLayout
        int row = 0;
        addFormField(formPanel, "Nom :", nomField, gbc, row++);
        addFormField(formPanel, "Prénom :", prenomField, gbc, row++);
        addFormField(formPanel, "Spécialité :", specialiteField, gbc, row++);
        addFormField(formPanel, "Email :", emailField, gbc, row++);
        addFormField(formPanel, "Mot de passe :", passwordField, gbc, row++);

        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        JButton saveButton = createStyledButton("Enregistrer", new Color(63, 81, 181));
        JButton cancelButton = createStyledButton("Annuler", new Color(158, 158, 158));

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> handleCancel());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        // Assembly
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        add(mainPanel);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(250, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(250, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, 
                            GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private void handleSave() {
        if (validateFields()) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
                String query = "INSERT INTO enseignant (Nom, Prénom, Spécialité, Email, password) VALUES (?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, nomField.getText());
                    pstmt.setString(2, prenomField.getText());
                    pstmt.setString(3, specialiteField.getText());
                    pstmt.setString(4, emailField.getText());
                    pstmt.setString(5, new String(passwordField.getPassword()));
                    
                    pstmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this,
                        "Enseignant ajouté avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    parentPanel.refreshTable();
                    returnToList();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ajout de l'enseignant",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty() ||
            prenomField.getText().trim().isEmpty() ||
            specialiteField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            passwordField.getPassword().length == 0) {
            
            JOptionPane.showMessageDialog(this,
                "Veuillez remplir tous les champs",
                "Erreur de validation",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    public static void createAndShowGUI(ListeEnseignants parent) {
        AjoutEnseignant ajoutEnseignant = new AjoutEnseignant(parent);
        Container topLevelContainer = parent.getTopLevelAncestor();
        if (topLevelContainer instanceof MainApplication) {
            MainApplication mainApp = (MainApplication) topLevelContainer;
            mainApp.addAjoutEnseignantPage(ajoutEnseignant);
            mainApp.showPanel("AjoutEnseignant");
        }
    }
    private void handleCancel() {
        Container topLevelContainer = SwingUtilities.getWindowAncestor(this);
        if (topLevelContainer instanceof MainApplication) {
            MainApplication mainApp = (MainApplication) topLevelContainer;
            mainApp.showPanel("ListeEnseignants");
        }
    }

    private void returnToList() {
        Container topLevelContainer = SwingUtilities.getWindowAncestor(this);
        if (topLevelContainer instanceof MainApplication) {
            MainApplication mainApp = (MainApplication) topLevelContainer;
            mainApp.showPanel("ListeEnseignants");
        }
    }
}
