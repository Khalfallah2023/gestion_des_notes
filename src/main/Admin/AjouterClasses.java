package main.Admin;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class AjouterClasses extends JPanel {
    private JTextField nomClasseField;
    private JTextField niveauField;
    private JTextField sectionField;
    private JButton addButton;
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Color BACKGROUND_COLOR = new Color(246, 246, 249);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);

    public AjouterClasses() {
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

        JLabel titleLabel = new JLabel("Ajouter Classe");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        addButton = new JButton("Ajouter");
        styleButton(addButton);
        headerPanel.add(addButton, BorderLayout.EAST);

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
        nomClasseField = createStyledTextField("Entrez le nom de la classe");
        niveauField = createStyledTextField("Entrez le niveau");
        sectionField = createStyledTextField("Entrez la section");

        // Add components to panel
        addFormRow(contentPanel, "Nom de la Classe", nomClasseField, gbc, 0);
        addFormRow(contentPanel, "Niveau", niveauField, gbc, 2);
        addFormRow(contentPanel, "Section", sectionField, gbc, 4);

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
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Même police que CARD_TEXT_FONT
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            button.getBorder(),
            new EmptyBorder(8, 15, 8, 15)
        ));
    }

    private void initializeActionListeners() {
        addButton.addActionListener(_ -> {
            String nomClasse = nomClasseField.getText().trim();
            String niveau = niveauField.getText().trim();
            String section = sectionField.getText().trim();
            
            if (nomClasse.isEmpty() || niveau.isEmpty() || section.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            ajouterClasse(nomClasse, niveau, section);
        });
    }

    private void ajouterClasse(String nomClasse, String niveau, String section) {
        String sql = "INSERT INTO classe(Nom_classe, Niveau, Section) VALUES(?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            // Chargement explicite du driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Établir la connexion
            conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
            
            // Désactiver l'auto-commit pour pouvoir rollback en cas d'erreur
            conn.setAutoCommit(false);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nomClasse);
            pstmt.setString(2, niveau);
            pstmt.setString(3, section);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Si l'insertion a réussi, on commit
                conn.commit();
                JOptionPane.showMessageDialog(this,
                    "Classe ajoutée avec succès !",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            } else {
                // Si aucune ligne n'a été affectée
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                    "Erreur: Aucune donnée n'a été insérée",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur: Driver MySQL non trouvé\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'ajout : " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void clearFields() {
        nomClasseField.setText("");
        niveauField.setText("");
        sectionField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Ajouter Classe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new AjouterClasses());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}