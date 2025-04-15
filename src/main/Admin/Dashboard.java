package main.Admin;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;  

import javax.swing.table.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import java.util.Comparator;
public class Dashboard extends JPanel {
    private static final String DB_URL = "jdbc:mysql://localhost/ensit";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font CARD_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    private static final Color BACKGROUND_COLOR = new Color(246, 246, 249);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(63, 81, 181);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    
   
    public Dashboard() {
        try {
            System.out.println("Starting Dashboard constructor");
            
            // Test la connexion à la base de données
            try (Connection conn = getConnection()) {
                System.out.println("Database connection test successful");
            } catch (SQLException e) {
                System.err.println("Database connection failed: " + e.getMessage());
                e.printStackTrace();
            }
            
            setLayout(new BorderLayout(20, 20));
            setBackground(new Color(246, 246, 249));
            setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            System.out.println("Initializing Dashboard components...");
            initializeComponents();
            System.out.println("Dashboard initialization complete");
            
        } catch (Exception e) {
            System.err.println("Error in Dashboard constructor: ");
            e.printStackTrace();
        }
    }

    private void initializeComponents() {
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Main content panel with scroll
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setOpaque(false);
        
        // Stats cards
        mainContent.add(createStatsPanel(), BorderLayout.NORTH);
        
        // Charts panel
        mainContent.add(createChartsPanel(), BorderLayout.CENTER);
        
        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
  
    private JPanel createChartsPanel() {
        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsPanel.setOpaque(false);
        
        chartsPanel.add(createDataCard("Répartition par section", getSectionData()));
        chartsPanel.add(createDataCard("Étudiants par niveau", getLevelData()));
        JPanel specialtyTablePanel = new ImprovedSpecialtyTable(getSpecialtyData());
        chartsPanel.add(specialtyTablePanel);
        return chartsPanel;
    }
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        
        statsPanel.add(createStatBox("Nombre d'enseignant", getTeacherCount()));
        statsPanel.add(createStatBox("Nombre d'élève", getStudentCount()));
        statsPanel.add(createStatBox("Nombre de classe", getClassCount()));
        
        return statsPanel;
    }
    
    private JPanel createStatBox(String title, int value) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(255, 255, 255));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        box.add(titleLabel);
        box.add(Box.createVerticalStrut(10));
        box.add(valueLabel);

        return box;
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Tableau de bord");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        header.add(titleLabel, BorderLayout.WEST);
        
        // Logout button
        JButton logoutButton = new JButton("Déconnexion");
        styleButton(logoutButton, DANGER_COLOR);
        logoutButton.addActionListener(e -> handleLogout());
        header.add(logoutButton, BorderLayout.EAST);
        
        return header;
    }

    private void handleLogout() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
            
            // Créer et afficher une nouvelle instance de LoginPage
            Platform.runLater(() -> {
                try {
                    Stage newStage = new Stage();
                    LoginPage loginPage = new LoginPage();
                    loginPage.start(newStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private JPanel createDataCard(String title, String[][] data) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
    
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        card.add(titleLabel, BorderLayout.NORTH);
    
        if (title.equals("Répartition par section")) {
            card.add(new PieChartPanel(data), BorderLayout.CENTER);
        } else if (title.equals("Étudiants par niveau")) {
            card.add(new BarChartPanel(data), BorderLayout.CENTER);
        } else {
            String[] columnNames = {"Catégorie", "Nombre"};
            JTable table = new JTable(data, columnNames);
            styleTable(table);
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            card.add(scrollPane, BorderLayout.CENTER);
        }
        
        return card;
    }

     private void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(CARD_TITLE_FONT);
        table.getTableHeader().setBackground(BACKGROUND_COLOR);
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.setSelectionBackground(new Color(237, 242, 249));
        table.setSelectionForeground(TEXT_COLOR);
        
        // Center align the "Nombre" column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }
    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    private String[][] getSectionData() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT c.Section, COUNT(e.ID_étudiant) as count " +
                "FROM classe c LEFT JOIN étudiant e ON c.ID_classe = e.ID_classe " +
                "GROUP BY c.Section"
            );
            
            return resultSetToArray(rs, "Section", "count");
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{{"Erreur", "0"}};
        }
    }

    private String[][] getLevelData() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT c.Niveau, COUNT(e.ID_étudiant) as count " +
                "FROM classe c LEFT JOIN étudiant e ON c.ID_classe = e.ID_classe " +
                "GROUP BY c.Niveau ORDER BY c.Niveau"
            );
            
            return resultSetToArray(rs, "Niveau", "count");
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{{"Erreur", "0"}};
        }
    }

    private String[][] getSpecialtyData() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT Spécialité, COUNT(*) as count " +
                "FROM enseignant WHERE Spécialité IS NOT NULL " +
                "GROUP BY Spécialité"
            );
            
            return resultSetToArray(rs, "Spécialité", "count");
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{{"Erreur", "0"}};
        }
    }

    private String[][] resultSetToArray(ResultSet rs, String labelColumn, String valueColumn) throws SQLException {
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        while (rs.next()) {
            String[] row = new String[2];
            row[0] = rs.getString(labelColumn);
            row[1] = String.valueOf(rs.getInt(valueColumn));
            rows.add(row);
        }
        return rows.toArray(new String[0][]);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private int getTeacherCount() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM enseignant");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getStudentCount() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM étudiant");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getClassCount() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM classe");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private static class PieChartPanel extends JPanel {
        private final ArrayList<Double> values;  // Changé List en ArrayList
        private final ArrayList<String> labels;  // Changé List en ArrayList
        private final ArrayList<Color> colors;   // Changé List en ArrayList
        private static final int LEGEND_SPACING = 20;
        private static final Color[] CHART_COLORS = {
            new Color(63, 81, 181),
            new Color(92, 107, 192),
            new Color(121, 134, 203),
            new Color(159, 168, 218),
            new Color(179, 186, 225),
            new Color(197, 202, 233),
            new Color(216, 219, 241)
        };

        public PieChartPanel(String[][] data) {
            values = new ArrayList<>();
            labels = new ArrayList<>();
            colors = new ArrayList<>();
            
            for (int i = 0; i < data.length; i++) {
                try {
                    double value = Double.parseDouble(data[i][1]);
                    if (value > 0) {
                        values.add(value);
                        labels.add(data[i][0]);
                        colors.add(CHART_COLORS[i % CHART_COLORS.length]);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erreur de conversion pour la valeur: " + data[i][1]);
                }
            }
            setPreferredSize(new Dimension(400, 300));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (values.isEmpty()) {
                g2d.drawString("Aucune donnée à afficher", getWidth()/2 - 50, getHeight()/2);
                return;
            }

            double total = 0;
            for (Double value : values) {
                total += value;
            }
            
            int diameter = Math.min(getWidth() - 150, getHeight() - 50);
            int x = 30;
            int y = (getHeight() - diameter) / 2;
            
            double currentAngle = 0;
            for (int i = 0; i < values.size(); i++) {
                double sliceAngle = (values.get(i) / total) * 360;
                g2d.setColor(colors.get(i));
                g2d.fillArc(x, y, diameter, diameter, (int) currentAngle, (int) sliceAngle);
                currentAngle += sliceAngle;
            }

            int legendX = x + diameter + 20;
            int legendY = y;
            Font legendFont = new Font("Segoe UI", Font.PLAIN, 12);
            g2d.setFont(legendFont);

            for (int i = 0; i < labels.size(); i++) {
                g2d.setColor(colors.get(i));
                g2d.fillRect(legendX, legendY + (i * LEGEND_SPACING), 15, 15);
                
                g2d.setColor(Color.BLACK);
                String percentage = String.format("%.1f%%", (values.get(i) / total) * 100);
                g2d.drawString(labels.get(i) + " (" + percentage + ")", 
                              legendX + 20, 
                              legendY + 12 + (i * LEGEND_SPACING));
            }
        }
    }
class BarChartPanel extends JPanel {
    private final ArrayList<Double> values;  
        private final ArrayList<String> labels;
    private static final Color BAR_COLOR = new Color(63, 81, 181);
    private static final Color BAR_HOVER_COLOR = new Color(92, 107, 192);
    private static final int BAR_WIDTH = 50;
    private static final int PADDING = 40;
    private static final int LABEL_PADDING = 20;

    public BarChartPanel(String[][] data) {
        values = new ArrayList<>();
        labels = new ArrayList<>();
        
        // Trier les données par niveau (1er, 2ème, 3ème)
        String[][] sortedData = data.clone();
        Arrays.sort(data, (a, b) -> {
            // Convertir "1er" en 1, "2ème" en 2, etc.
            int aNum = extractNumber(a[0]);
            int bNum = extractNumber(b[0]);
            return Integer.compare(aNum, bNum);
        });

        // Filtrer et ajouter les données valides
        for (String[] row : sortedData) {
            try {
                double value = Double.parseDouble(row[1]);
                if (value > 0) {
                    values.add(value);
                    labels.add(row[0]);
                }
            } catch (NumberFormatException e) {
                System.err.println("Erreur de conversion pour la valeur: " + row[1]);
            }
        }
        setPreferredSize(new Dimension(400, 300));
    }

    private int extractNumber(String level) {
        try {
            if (level.toLowerCase().startsWith("1")) return 1;
            if (level.toLowerCase().startsWith("2")) return 2;
            if (level.toLowerCase().startsWith("3")) return 3;
            return Integer.MAX_VALUE;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (values.isEmpty()) {
            g2d.drawString("Aucune donnée à afficher", getWidth()/2 - 50, getHeight()/2);
            return;
        }

        // Calculer les dimensions
        int chartWidth = getWidth() - (2 * PADDING);
        int chartHeight = getHeight() - (2 * PADDING) - LABEL_PADDING;
        
        // Trouver la valeur maximum et arrondir au multiple de 50 supérieur
        double maxValue = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        maxValue = Math.ceil(maxValue / 50) * 50;
        
        // Dessiner les axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(PADDING, PADDING, PADDING, getHeight() - PADDING - LABEL_PADDING);
        g2d.drawLine(PADDING, getHeight() - PADDING - LABEL_PADDING, 
                     getWidth() - PADDING, getHeight() - PADDING - LABEL_PADDING);

        // Dessiner les graduations sur l'axe Y
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= maxValue; i += 50) {
            int y = getHeight() - PADDING - LABEL_PADDING - (int)((i / maxValue) * chartHeight);
            g2d.drawLine(PADDING - 5, y, PADDING, y);
            g2d.drawString(String.valueOf(i), PADDING - 35, y + 5);
        }

        // Dessiner les barres
        int barSpacing = chartWidth / (values.size() + 1);
        for (int i = 0; i < values.size(); i++) {
            int barHeight = (int)((values.get(i) / maxValue) * chartHeight);
            int x = PADDING + ((i + 1) * barSpacing) - (BAR_WIDTH / 2);
            int y = getHeight() - PADDING - LABEL_PADDING - barHeight;
            
            // Dessiner la barre avec dégradé
            GradientPaint gradient = new GradientPaint(
                x, y, BAR_COLOR,
                x + BAR_WIDTH, y + barHeight, BAR_HOVER_COLOR
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, BAR_WIDTH, barHeight);
            
            // Valeur au-dessus de la barre
            g2d.setColor(Color.BLACK);
            String value = String.valueOf(values.get(i).intValue());
            FontMetrics fm = g2d.getFontMetrics();
            int valueWidth = fm.stringWidth(value);
            g2d.drawString(value, x + (BAR_WIDTH - valueWidth) / 2, y - 5);
            
            // Label sous la barre
            String label = labels.get(i);
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, 
                          x + (BAR_WIDTH - labelWidth) / 2,
                          getHeight() - PADDING + 5);
        }
    }
}

class ImprovedSpecialtyTable extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField searchField;
    private final Color HEADER_BG = new Color(63, 81, 181);
    private final Color HEADER_FG = Color.WHITE;
    private final Color ALTERNATE_ROW_COLOR = new Color(245, 245, 250);
    
    public ImprovedSpecialtyTable(String[][] data) {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        
        // Search icon and field
        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Rechercher une spécialité...");
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Create table model with column names
        String[] columnNames = {"Spécialité", "Nombre d'enseignants", "Pourcentage"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Integer.class;
                if (column == 2) return Double.class;
                return String.class;
            }
        };
        
        // Calculate total teachers and percentages
        int totalTeachers = 0;
        for (String[] row : data) {
            totalTeachers += Integer.parseInt(row[1]);
        }
        
        // Add data with percentages
        for (String[] row : data) {
            int count = Integer.parseInt(row[1]);
            double percentage = (count * 100.0) / totalTeachers;
            model.addRow(new Object[]{
                row[0],
                count,
                percentage
            });
        }
        
        // Create and configure table
        table = new JTable(model);
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowVerticalLines(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Custom header renderer
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                label.setBackground(HEADER_BG);
                label.setForeground(HEADER_FG);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
                return label;
            }
        });
        
        // Custom cell renderer for alternating rows and formatting
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_COLOR);
                }
                
                // Format numbers
                if (value instanceof Integer) {
                    setText(String.format("%d", (Integer)value));
                } else if (value instanceof Double) {
                    setText(String.format("%.1f%%", (Double)value));
                }
                
                // Center align numbers
                setHorizontalAlignment(column > 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                
                return c;
            }
        };
        
        // Apply renderer to all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        
        // Make columns sortable
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        // Add search functionality
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
            
            private void search() {
                String text = searchField.getText().trim().toLowerCase();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
                }
            }
        });
        
        // Add components to panel
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Add total row at bottom
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel(String.format(
            "Total des enseignants: %d", totalTeachers
        )));
        add(totalPanel, BorderLayout.SOUTH);
    }
}}