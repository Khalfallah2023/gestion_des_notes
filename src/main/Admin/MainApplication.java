package main.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainApplication extends JFrame {
    private static final Color SIDEBAR_BG = new Color(146,161,207);
    private static final Color BUTTON_BG = new Color(50, 81, 181);
    private static final Color BUTTON_HOVER_BG = new Color(70, 75, 95);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font LOGO_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    private final CardLayout cardLayout;
    private final JPanel mainContent;
    private JPanel sidebar;
    private DepartementPanel departementPanel;
    private ModificationEnseignant modificationEnseignantPage;
    private ListeEnseignants enseignantsPage;
    
    private volatile boolean initialized = false;
   
    private final CountDownLatch initLatch = new CountDownLatch(1);
    
    public MainApplication() {
        setupMainFrame();
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(mainContent, BorderLayout.CENTER);
        System.out.println("**********");
        // S'assurer que JavaFX est initialisé
        initializePages();
        add(mainPanel);
    }

    
    private void setupMainFrame() {
        setTitle("Gestionnaire ENSIT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 0, 0, 0));
        addLogo();
        addMenuItems();
    }
    public void showPanel(String panelName) {
        System.out.println("Attempting to show panel: " + panelName); // Add debug log
        if (!initialized) {
            try {
                System.out.println("Waiting for initialization to complete...");
                initLatch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Initialization wait interrupted");
                return;
            }
        }
        
        if (!initialized) {
            System.err.println("Warning: Pages not initialized after timeout");
        }
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("Showing panel: " + panelName); // Add debug log
            cardLayout.show(mainContent, panelName);
        });
    }

    private void addLogo() {
        JLabel logo = new JLabel("Gestionnaire");
        logo.setFont(LOGO_FONT);
        logo.setForeground(TEXT_COLOR);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));
    }

    private void addMenuItems() {
        String[] menuItems = {"Dashboard", "ListeEnseignants", "AjouterClasse", "Matières", "Départements"};
        for (String item : menuItems) {
            if (item.equals("Départements")) {
                addDepartmentsMenu();
            } else {
                addRegularMenuItem(item);
            }
        }
    }

    private void addRegularMenuItem(String item) {
        JButton menuButton = createMenuButton(item);
        menuButton.addActionListener(e -> {
        System.out.println("Clicking menu item: " + item);
        showPanel(item);
    }); 
        sidebar.add(menuButton);
        sidebar.add(Box.createVerticalStrut(10));
    }
    public ListeEnseignants getEnseignantsPage() {
        return enseignantsPage;
    }
    private void addDepartmentsMenu() {
        JButton depButton = createMenuButton("Départements");
        JPopupMenu sectionMenu = createSectionsPopupMenu();
        
        depButton.addActionListener(e -> {
            cardLayout.show(mainContent, "Départements");
            sectionMenu.show(depButton, 0, depButton.getHeight());
        });
        
        sidebar.add(depButton);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private JPopupMenu createSectionsPopupMenu() {
        JPopupMenu sectionMenu = new JPopupMenu();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root")) {
            loadSectionsFromDB(conn, sectionMenu);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
        return sectionMenu;
    }

    private void loadSectionsFromDB(Connection conn, JPopupMenu sectionMenu) throws SQLException {
        String sql = "SELECT DISTINCT Section FROM classe ORDER BY Section";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String section = rs.getString("Section");
                JMenu sectionSubMenu = createSectionSubmenu(conn, section);
                sectionMenu.add(sectionSubMenu);
            }
        }
    }

    private JMenu createSectionSubmenu(Connection conn, String section) throws SQLException {
        JMenu sectionSubMenu = new JMenu(section);
        String niveauxSql = "SELECT DISTINCT Niveau FROM classe WHERE Section = ? ORDER BY Niveau";
        
        try (PreparedStatement niveauxStmt = conn.prepareStatement(niveauxSql)) {
            niveauxStmt.setString(1, section);
            try (ResultSet niveauxRs = niveauxStmt.executeQuery()) {
                while (niveauxRs.next()) {
                    String niveau = niveauxRs.getString("Niveau");
                    JMenu niveauItem = createNiveauSubmenu(conn, section, niveau);
                    sectionSubMenu.add(niveauItem);
                }
            }
        }
        return sectionSubMenu;
    }

    private JMenu createNiveauSubmenu(Connection conn, String section, String niveau) throws SQLException {
        JMenu niveauItem = new JMenu(niveau);
        String classeSql = "SELECT Nom_classe FROM classe WHERE Section = ? AND Niveau = ?";
        
        try (PreparedStatement classeStmt = conn.prepareStatement(classeSql)) {
            classeStmt.setString(1, section);
            classeStmt.setString(2, niveau);
            try (ResultSet classeRs = classeStmt.executeQuery()) {
                while (classeRs.next()) {
                    String nomClasse = classeRs.getString("Nom_classe");
                    JMenuItem classItem = new JMenuItem(nomClasse);
                    classItem.addActionListener(e -> showClassDetails(nomClasse));
                    niveauItem.add(classItem);
                }
            }
        }
        return niveauItem;
    }
    public ModificationEnseignant getModificationEnseignantPage() {
        return modificationEnseignantPage;
    }
    public void addAjoutEnseignantPage(AjoutEnseignant ajoutEnseignant) {
        mainContent.add(ajoutEnseignant, "AjoutEnseignant");
        mainContent.revalidate();
        mainContent.repaint();
    }
    private void initializePages() {
    System.out.println("Initializing pages...");
    // Initialize Swing components first
    try {// Create and set ListeEnseignants scene
        System.out.println("Creating Dashboard instance...");
        Dashboard dashboard = new Dashboard();
        System.out.println("Dashboard instance created successfully");
        System.out.println("Creating ListeEnseignants instance...");
        enseignantsPage = new ListeEnseignants();
        enseignantsPage.setPreferredSize(new Dimension(800, 600));
        System.out.println("ListeEnseignants instance created successfully");
        System.out.println("-********Creating other components...******");
        departementPanel = new DepartementPanel();
        modificationEnseignantPage =new ModificationEnseignant();
        ListeMatieres matierePage = new ListeMatieres();
        AjouterClasses classesPage = new AjouterClasses();
        AjoutEnseignant ajoutEnseignantPage = new AjoutEnseignant(enseignantsPage);
        // Add Swing components on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("Adding components to mainContent...");
                mainContent.add(dashboard, "Dashboard");
                mainContent.add(enseignantsPage, "ListeEnseignants");
                mainContent.add(departementPanel, "Départements");
                mainContent.add(matierePage, "Matières");
                mainContent.add(ajoutEnseignantPage, "AjoutEnseignant"); 
                mainContent.add(classesPage, "AjouterClasse");
                mainContent.add(modificationEnseignantPage, "ModificationEnseignant");
                mainContent.revalidate();
                mainContent.repaint();
                System.out.println("Showing Dashboard...");
                cardLayout.show(mainContent, "Dashboard");
                
                initialized = true;
                initLatch.countDown();
                System.out.println("All pages initialized successfully");
            } catch (Exception e) {
                System.err.println("Error in EDT: ");
                e.printStackTrace();
            }
        });}catch (Exception e) {
            System.err.println("Error in initializePages: ");
            e.printStackTrace();
            initLatch.countDown();
        }
    }
       
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBackground(BUTTON_BG);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(MENU_FONT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_BG);
            }
        });
        
        return button;
    }

    private void showClassDetails(String nomClasse) {
        System.out.println("Tentative d'affichage de la classe: " + nomClasse);
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/ensit", "root", "root");
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM classe WHERE Nom_classe = ?")) {
            
            stmt.setString(1, nomClasse);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    if (departementPanel != null) {
                        System.out.println("Mise à jour de la classe: " + nomClasse);
                        departementPanel.setCurrentClass(nomClasse);
                    } else {
                        System.out.println("Erreur: departementPanel est null");
                    }
                } else {
                    showError("La classe " + nomClasse + " n'existe pas dans la base de données.");
                }
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        showError("Erreur de base de données: " + e.getMessage());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public void handleLogout() {
        dispose();
        Platform.runLater(() -> {
            try {
                new LoginPage().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        // Initialize JavaFX toolkit
        Platform.setImplicitExit(false);
        new JFXPanel(); // Create a JFXPanel to initialize JavaFX toolkit
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        SwingUtilities.invokeLater(() -> {
            MainApplication app = new MainApplication();
            app.setVisible(true);
        });
    }}