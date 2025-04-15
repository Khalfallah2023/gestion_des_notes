package main.Admin;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class ModificationEnseignant extends JPanel {
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField emailField;
    private JTextField specialiteField;
    private JButton saveButton;
    private String currentEnseignantId;
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Color BACKGROUND_COLOR = new Color(246, 246, 249);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);

    public ModificationEnseignant() {
        // Panel configuration
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Create and add the main components
        JPanel contentPanel = createContentPanel();
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        initializeActionListeners();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Modifier l'enseignant");
        titleLabel.setFont(TITLE_FONT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        saveButton = new JButton("Enregistrer");
        styleButton(saveButton);
        headerPanel.add(saveButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // Create form fields
        nomField = createStyledTextField("Entrez le nom");
        prenomField = createStyledTextField("Entrez le prénom");
        emailField = createStyledTextField("Entrez l'email");
        specialiteField = createStyledTextField("Entrez la spécialité");

        // Add components to panel
        addFormRow(contentPanel, "Nom", nomField, gbc, 0);
        addFormRow(contentPanel, "Prénom", prenomField, gbc, 2);
        addFormRow(contentPanel, "Email", emailField, gbc, 4);
        addFormRow(contentPanel, "Spécialité", specialiteField, gbc, 6);

        return contentPanel;
    }

    private void addFormRow(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int gridy) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(86, 101, 115));

        gbc.gridy = gridy;
        panel.add(label, gbc);

        gbc.gridy = gridy + 1;
        panel.add(field, gbc);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(15);
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            button.getBorder(),
            new EmptyBorder(8, 15, 8, 15)
        ));
    }

    public void setEnseignantData(String[] enseignant) {
        currentEnseignantId = enseignant[0];
        nomField.setText(enseignant[1]);
        prenomField.setText(enseignant[2]);
        specialiteField.setText(enseignant[3]);
        emailField.setText(enseignant[4]);
    }

    private void initializeActionListeners() {
        saveButton.addActionListener(_ -> {
            if (validateFields()) {
                saveModifications();
            }
        });
    }

    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty() || 
            prenomField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() || 
            specialiteField.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this,
                "Veuillez remplir tous les champs",
                "Erreur",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveModifications() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            String sql = "UPDATE enseignant SET Nom=?, Prénom=?, Email=?, Spécialité=? WHERE ID_enseignant=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nomField.getText().trim());
                pstmt.setString(2, prenomField.getText().trim());
                pstmt.setString(3, emailField.getText().trim());
                pstmt.setString(4, specialiteField.getText().trim());
                pstmt.setString(5, currentEnseignantId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Modifications enregistrées avec succès",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Return to teachers list
                    Container topLevelContainer = this.getTopLevelAncestor();
                    if (topLevelContainer instanceof MainApplication) {
                        MainApplication mainApp = (MainApplication) topLevelContainer;
                        mainApp.showPanel("ListeEnseignants");
                        if (mainApp.getEnseignantsPage() != null) {
                            mainApp.getEnseignantsPage().refreshTable();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification : " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}