package main.Admin;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;


public class ListeMatieres  extends JPanel{
   private static final Color BACKGROUND_COLOR = new Color(246, 246, 249);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Color HOVER_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);

    private JPanel cardsPanel;
    private JFrame parentFrame;
    public ListeMatieres() {
        initializeJDBC();
        setupUI();
    }
    private  void initializeJDBC() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            System.exit(1);
        }
    }
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Liste des Matières");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(ACCENT_COLOR);
        JButton addButton = new JButton("Ajouter une matière");
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
        
        addButton.addActionListener(_ -> showAddMatiereDialog());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Cards Panel
        cardsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        cardsPanel.setBackground(BACKGROUND_COLOR);
        
        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Initial load
        loadMatieresFromDB();
    }


    
    private void showAddMatiereDialog() {
        parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "Ajouter une matière", true);
        
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 300);
       
    
        // Background panel
        JPanel backgroundPanel = new JPanel(new BorderLayout(10, 10));
        backgroundPanel.setBackground(BACKGROUND_COLOR);
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(backgroundPanel, BorderLayout.CENTER);
    
        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
    
        // Fields
        JLabel nomLabel = new JLabel("Nom :");
        nomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField nomField = new JTextField(20);
        nomField.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
    
        JLabel descLabel = new JLabel("Description :");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null);
    
        // Layout fields
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(nomLabel, gbc);
        gbc.gridx = 1;
        form.add(nomField, gbc);
    
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(descLabel, gbc);
        gbc.gridx = 1;
        form.add(descScroll, gbc);
    
        backgroundPanel.add(form, BorderLayout.CENTER);
    // Save button
JButton saveButton = new JButton("Enregistrer");
saveButton.setBackground(ACCENT_COLOR);
saveButton.setForeground(Color.WHITE);
saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
saveButton.setFocusPainted(false);
saveButton.setBorderPainted(false);
saveButton.setContentAreaFilled(true);
saveButton.setOpaque(true);
saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
saveButton.setBorder(BorderFactory.createCompoundBorder(
    saveButton.getBorder(), 
    new EmptyBorder(8, 15, 8, 15)
));

saveButton.addActionListener(_ -> {
    if (saveMatiereToDb(nomField.getText(), descArea.getText())) {
        dialog.dispose();
        loadMatieresFromDB();
    }
});


       
    
        // Hover effect on button
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveButton.setBackground(HOVER_COLOR);
            }
    
            @Override
            public void mouseExited(MouseEvent e) {
                saveButton.setBackground(ACCENT_COLOR);
            }
        });
    
        backgroundPanel.add(saveButton, BorderLayout.SOUTH);
    
        dialog.setVisible(true);
    }
    
   public void loadMatieresFromDB() {
        cardsPanel.removeAll();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID_matière, Nom_matière, Description FROM matière")) {

            while (rs.next()) {
                cardsPanel.add(new ModernCard(
                    String.valueOf(rs.getInt("ID_matière")),
                    rs.getString("Nom_matière"),
                    rs.getString("Description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur de connexion à la base de données: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    private boolean saveMatiereToDb(String nom, String description) {
        String sql = "INSERT INTO matière (Nom_matière, Description) VALUES (?, ?)";
        
        // Obtenir le frame parent
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nom);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            
            // Utiliser parentFrame au lieu de mainFrame
            JOptionPane.showMessageDialog(parentFrame, 
                "Matière ajoutée avec succès!",
                "Succès",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Recharger la liste des matières après l'ajout
            loadMatieresFromDB();
            
            return true;
            
        } catch (SQLException e) {
            // Utiliser parentFrame au lieu de mainFrame
            JOptionPane.showMessageDialog(parentFrame, 
                "Erreur lors de l'ajout: " + e.getMessage(),
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
                
            return false;
        }
    }
    
}
class RoundedBorder extends AbstractBorder {
    private final int radius;
    private final Color color;
    private final Insets insets;
    
    public RoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
        this.insets = new Insets(radius/2, radius/2, radius/2, radius/2);
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }
    
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
        g2d.dispose();
    }
}

    

    
    
    
        
          

   
    

   
class ModernCard extends JPanel {
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);
    private static final Color HOVER_COLOR = new Color(245, 247, 255);
    private final String id;
    private final String nom;
    private final String description;
    private final JLabel nameLabel;
    private final JTextArea descArea;
    private JTextField nomField;
     private JTextArea newDescArea;
    public ModernCard(String id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.nameLabel = new JLabel(nom);
        this.descArea = new JTextArea(description);
        
        
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, Color.LIGHT_GRAY),
            new EmptyBorder(15, 20, 15, 20)
        ));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createDescriptionPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.EAST);
        
        addHoverEffects();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setOpaque(false);
        
        JLabel idLabel = new JLabel("#" + id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(ACCENT_COLOR);
        
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameLabel.setForeground(Color.DARK_GRAY);
        
        headerPanel.add(idLabel, BorderLayout.WEST);
        headerPanel.add(nameLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    private JPanel createDescriptionPanel() {
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        descPanel.add(descArea, BorderLayout.CENTER);
        
        return descPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(createEditButton());
        buttonPanel.add(createDeleteButton());
        
        return buttonPanel;
    }

    private JButton createEditButton() {
        JButton editButton = createIconButton("src/main/images/social.png", "Modifier cette matière");
        editButton.addActionListener(_ -> showEditDialog());
        return editButton;
    }

    private JButton createDeleteButton() {
        JButton deleteButton = createIconButton("src/main/images/delete.png", "Supprimer cette matière");
        deleteButton.addActionListener(_ -> handleDelete());
        return deleteButton;
    }

    private JButton createIconButton(String iconPath, String tooltip) {
        JButton button = new JButton();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            button.setText(tooltip); // Fallback if image not found
        }
        
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        return button;
    }

    private void showEditDialog() {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                   "Modifier la matière", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        nomField = new JTextField(nom);
        newDescArea = new JTextArea(description);
        
        JPanel form = createEditForm(dialog);
        dialog.add(form, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         JButton saveButton = createSaveButton(dialog, nomField, newDescArea); 
         buttonPanel.add(saveButton);
          dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createEditForm(JDialog dialog) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
       
        nomField.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        addFormField(form, "Nom :", nomField, gbc, 0);

        // Description field
        
        newDescArea.setLineWrap(true);
        newDescArea.setWrapStyleWord(true);
        newDescArea.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        JScrollPane scrollPane = new JScrollPane(newDescArea);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        addFormField(form, "Description :", scrollPane, gbc, 1);

       

        return form;
    }

    private void addFormField(JPanel form, String labelText, JComponent field, 
                            GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(new JLabel(labelText), gbc);
        
        gbc.gridx = 1;
        form.add(field, gbc);
    }

    private JButton createSaveButton(JDialog dialog, JTextField nomField, JTextArea newDescArea) {
        JButton saveButton = new JButton("Enregistrer");
        saveButton.setBackground(ACCENT_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(_-> {
            if (updateMatiere(nomField.getText(), descArea.getText())) {
                dialog.dispose();
                System.out.println("Nom avant mise à jour de l'interface: " +nameLabel.getText());
                 nameLabel.setText(nomField.getText()); 
                 System.out.println("Nom après mise à jour de l'interface: " + nameLabel.getText());
                 System.out.println("desc avant mise à jour de l'interface: " + descArea.getText());
                
                 System.out.println("desc après mise à jour de l'interface: " + descArea.getText());
                 nameLabel.setText(newDescArea.getText()); 
                // Actualiser l'interface utilisateur
                 SwingUtilities.invokeLater(() -> {
                     nameLabel.revalidate();
                     nameLabel.repaint();
                     descArea.revalidate();
                     descArea.repaint();
                    });
            }
        });
        return saveButton;
    }

    private void handleDelete() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Voulez-vous vraiment supprimer cette matière ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            supprimerMatiere();
        }
    }

    private boolean updateMatiere(String nom, String description) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE matière SET Nom_matière = ?, Description = ? WHERE ID_matière = ?")) {
            
            pstmt.setString(1, nom);
            pstmt.setString(2, description);
            pstmt.setString(3, id);
            pstmt.executeUpdate();
            // Mettre à jour l'interface immédiatement
        nameLabel.setText(nom);
        descArea.setText(description);
        //refraich
        Container parent = getParent();
        if (parent instanceof ListeMatieres) {
            ((ListeMatieres) parent).loadMatieresFromDB();
        }
            JOptionPane.showMessageDialog(this, 
                "Matière mise à jour avec succès!", 
                "Succès", 
                JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la mise à jour: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void supprimerMatiere() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM matière WHERE ID_matière = ?")) {
            
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            
            SwingUtilities.invokeLater(() -> {
                Container parent = getParent();
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
            });
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la suppression: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }


    private void addHoverEffects() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animateBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                animateBackground(Color.WHITE);
            }
        });
    }

    private void animateBackground(Color targetColor) {
        Timer timer = new Timer(20, null);
        Color startColor = getBackground();
        int steps = 20;
        final int[] currentStep = {0};
        
        timer.addActionListener(_ -> {
            currentStep[0]++;
            float ratio = (float) currentStep[0] / steps;
            
            setBackground(new Color(
                interpolateColor(startColor.getRed(), targetColor.getRed(), ratio),
                interpolateColor(startColor.getGreen(), targetColor.getGreen(), ratio),
                interpolateColor(startColor.getBlue(), targetColor.getBlue(), ratio)
            ));
            
            if (currentStep[0] >= steps) {
                timer.stop();
            }
        });
        
        timer.start();
    }

    private int interpolateColor(int start, int end, float ratio) {
        return Math.round(start + (end - start) * ratio);
    }

      
   
    
}