package main.Admin;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.io.File;

public class ListeEnseignants extends JPanel {
    private static final String DB_URL = "jdbc:mysql://localhost/ensit";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);;
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font CARD_TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);

    private JPanel gridPanel;
    private int currentRow = 0;
    private static final int COLUMNS = 3;

    public ListeEnseignants() {
        System.out.println("ListeEnseignants constructor started");
    setPreferredSize(new Dimension(800, 600));
    setMinimumSize(new Dimension(800, 600));
    initialize();
    setVisible(true);
    System.out.println("ListeEnseignants constructor completed");
    }

    private void initialize() {
        
            System.out.println("Initializing ListeEnseignants...");
            
            if (!testDatabaseConnection()) {
                System.err.println("Database connection failed, aborting initialization");
                return;
            }
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(246, 246, 249));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and add header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Initialize grid panel
        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(new Color(246, 246, 249));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(246, 246, 249));
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        // Load teachers
        loadTeachers();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setBackground(new Color(246, 246, 249));

        JLabel titleLabel = new JLabel("Liste des Enseignants");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        JButton createButton = new JButton("Ajouter Enseignant");
        createButton.setBackground(new Color(63, 81, 181));
        createButton.setForeground(Color.WHITE);
       createButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        createButton.setBorderPainted(false);
        createButton.setFocusPainted(false);
        createButton.setContentAreaFilled(true);
        createButton.setOpaque(true);
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createButton.setBorder(BorderFactory.createCompoundBorder(
        createButton.getBorder(), 
        new EmptyBorder(8, 15, 8, 15)));
        createButton.addActionListener(e -> {
            Container topLevelContainer = SwingUtilities.getWindowAncestor(this);
            if (topLevelContainer instanceof MainApplication) {
                MainApplication mainApp = (MainApplication) topLevelContainer;
                mainApp.showPanel("AjoutEnseignant");
            }
        });

        headerPanel.add(titleLabel,BorderLayout.WEST);
        headerPanel.add(createButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void loadTeachers() {
        System.out.println("Loading teachers started");
        gridPanel.removeAll();
        ArrayList<String[]> enseignants = fetchEnseignantsFromDB();
        
        System.out.println("Number of teachers fetched: " + enseignants.size());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
    
        for (int i = 0; i < enseignants.size(); i++) {
            gbc.gridx = i % COLUMNS;
            gbc.gridy = i / COLUMNS;
            JPanel card = createModernCard(enseignants.get(i));
            gridPanel.add(card, gbc);
            System.out.println("Added teacher card: " + enseignants.get(i)[1] + " " + enseignants.get(i)[2]);
        }
    
        // Add empty panels to fill the last row if needed
        while (gbc.gridx < COLUMNS - 1) {
            gbc.gridx++;
            gridPanel.add(Box.createHorizontalStrut(200), gbc);
        }
    
        gridPanel.revalidate();
        gridPanel.repaint();
        
        // Forcer la mise à jour du panneau principal
        revalidate();
        repaint();
        
        System.out.println("Loading teachers completed");
    }

    private ArrayList<String[]> fetchEnseignantsFromDB() {
        System.out.println("Fetching teachers from database...");
        ArrayList<String[]> enseignants = new ArrayList<>();
        
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connection successful");
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT ID_enseignant, Nom, Prénom, Spécialité, Email FROM enseignant")) {
                
                System.out.println("Query executed successfully");
                
                while (rs.next()) {
                    String id = rs.getString("ID_enseignant");
                    String nom = rs.getString("Nom");
                    String prenom = rs.getString("Prénom");
                    String specialite = rs.getString("Spécialité");
                    String email = rs.getString("Email");
    
                    if (nom != null && !nom.isEmpty() && prenom != null && !prenom.isEmpty()) {
                        enseignants.add(new String[]{id, nom, prenom, specialite, email});
                        System.out.println("Added teacher: " + nom + " " + prenom);
                    }
                }
                
                System.out.println("Total teachers fetched: " + enseignants.size());
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur lors de la récupération des enseignants: " + e.getMessage());
        }
        return enseignants;
    }
    private boolean testDatabaseConnection() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Database connection test successful");
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            showErrorDialog("Erreur de connexion à la base de données: " + e.getMessage());
            return false;
        }
    }

    private JPanel createModernCard(String[] enseignant) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Avatar panel
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(63, 81, 181));
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                String initials = enseignant[1].substring(0, 1) + enseignant[2].substring(0, 1);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(initials, x, y);
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(enseignant[1] + " " + enseignant[2]);
        nameLabel.setFont(CARD_TITLE_FONT);

        JLabel emailLabel = new JLabel(enseignant[4]);
        emailLabel.setFont(CARD_TEXT_FONT);
        emailLabel.setForeground(Color.GRAY);

        JLabel specialityLabel = new JLabel(enseignant[3]);
        specialityLabel.setFont(CARD_TEXT_FONT);
        specialityLabel.setForeground(new Color(63, 81, 181));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(emailLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(specialityLabel);

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBackground(Color.WHITE);

        JButton editButton = createIconButton("src/main/imeges/social.png");
        JButton deleteButton = createIconButton("src/main/imeges/delete.png");
        editButton.addActionListener(e -> {
            // Récupérer l'instance de MainApplication
            Container topLevelContainer = this.getTopLevelAncestor();
            if (topLevelContainer instanceof MainApplication) {
                MainApplication mainApp = (MainApplication) topLevelContainer;
                // Appeler showPanel avec le nom du panneau de modification
                mainApp.showPanel("ModificationEnseignant");
                // Si vous avez besoin de passer les données de l'enseignant à modifier
                if (mainApp.getModificationEnseignantPage() != null) {
                    mainApp.getModificationEnseignantPage().setEnseignantData(enseignant);
                }
            }
        });
        deleteButton.addActionListener(e -> deleteTeacher(enseignant[0]));

        actionsPanel.add(editButton);
        actionsPanel.add(deleteButton);

        // Add components to card
        card.add(avatarPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionsPanel, BorderLayout.EAST);

        return card;
    }

    private JButton createIconButton(String imagePath) {
        JButton button = new JButton();
        System.out.println("Tentative de chargement de l'image: " + imagePath);
        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                System.out.println("Le fichier n'existe pas: " + file.getAbsolutePath());
                throw new Exception("Image non trouvée");
            }
            Image img = ImageIO.read(file);
            Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            e.printStackTrace();
            button.setText("X");
        }
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    private void deleteTeacher(String id) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer cet enseignant ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = connection.prepareStatement("DELETE FROM enseignant WHERE ID_enseignant = ?")) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                showInfoDialog("Enseignant supprimé avec succès");
                loadTeachers();
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorDialog("Erreur lors de la suppression de l'enseignant");
            }
        }
    }

    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Succès",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public void refreshTable() {
        loadTeachers();
    }
}