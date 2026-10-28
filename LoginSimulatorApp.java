import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginSimulatorApp extends JFrame {

    // Simulated Database
    private final Map<String, String> userDatabase = new HashMap<>();

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentTargetPassword = "";
    private String currentUsername = "";
    private String targetHash = "";

    // Decryption Screen Components
    private JLabel displayLabel, statusLabel, modeLabel, targetHashLabel, currentHashLabel, attemptsLabel, statsLabel, backtracksLabel;
    private ModernButton proceedButton;
    
    private Timer timer;
    private char[] currentDisplay;
    private int lockedIndex = 0;
    private int animationCycle = 0;
    private long attemptsCount = 0;
    private long backtrackCount = 0; // New Backtracking Counter
    private final Random random = new Random();
    private final String SCRAMBLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private Clip clickClip;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    
    // Modern UI Color Palette
    private final Color APP_BG = new Color(240, 242, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color PRIMARY_BLUE = new Color(24, 119, 242);
    private final Color PRIMARY_HOVER = new Color(21, 101, 204);
    private final Color SECONDARY_BTN = new Color(228, 230, 235);
    private final Color SECONDARY_HOVER = new Color(216, 218, 223);
    private final Color TEXT_DARK = new Color(28, 30, 33);
    private final Color TEXT_MUTED = new Color(96, 103, 112);

    public LoginSimulatorApp() {
        setTitle("Smart Parking System - Secure Access");
        setSize(850, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(APP_BG);

        loadSound();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(APP_BG);

        mainPanel.add(buildLoginPanel(), "LOGIN");
        mainPanel.add(buildRegisterPanel(), "REGISTER");
        mainPanel.add(buildCrackerPanel(), "CRACKER");
        mainPanel.add(buildDashboardPanel(), "DASHBOARD");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN"); 
    }

    private void loadSound() {
        try {
            File soundFile = new File("click.wav");
            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                clickClip = AudioSystem.getClip();
                clickClip.open(audioIn);
            }
        } catch (Exception ignored) {}
    }

    private void playClickSound() {
        if (clickClip != null) {
            clickClip.setFramePosition(0);
            clickClip.start();
        }
    }

    // --- SCREEN 1: LOGIN PANEL ---
    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout()); 
        wrapper.setBackground(APP_BG);

        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 480));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTitle = new JLabel("Sign in to continue to Dashboard");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(TEXT_MUTED);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField idField = createModernTextField("User ID");
        JPasswordField passField = (JPasswordField) createModernTextField("Password");

        ModernButton loginBtn = new ModernButton("Login", PRIMARY_BLUE, Color.WHITE, PRIMARY_HOVER);
        ModernButton registerBtn = new ModernButton("Create New Account", SECONDARY_BTN, TEXT_DARK, SECONDARY_HOVER);
        
        JButton crackBtn = new JButton("Initiate System Recovery / Audit");
        crackBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        crackBtn.setForeground(new Color(220, 53, 69));
        crackBtn.setContentAreaFilled(false);
        crackBtn.setBorderPainted(false);
        crackBtn.setFocusPainted(false);
        crackBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        crackBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pass = new String(passField.getPassword());
            if (userDatabase.containsKey(id) && userDatabase.get(id).equals(pass)) {
                currentUsername = id;
                cardLayout.show(mainPanel, "DASHBOARD");
                idField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid ID or Password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "REGISTER"));

        crackBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a User ID first.", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else if (!userDatabase.containsKey(id)) {
                JOptionPane.showMessageDialog(this, "User ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                currentUsername = id;
                currentTargetPassword = userDatabase.get(id);
                targetHash = generateMockHash(currentTargetPassword);
                prepareCrackerUI();
                cardLayout.show(mainPanel, "CRACKER");
                startSpinAnimation();
                idField.setText("");
                passField.setText("");
            }
        });

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subTitle);
        card.add(Box.createVerticalStrut(35));
        card.add(createLabel("User ID"));
        card.add(idField);
        card.add(Box.createVerticalStrut(15));
        card.add(createLabel("Password"));
        card.add(passField);
        card.add(Box.createVerticalStrut(25));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(10));
        card.add(crackBtn);

        wrapper.add(card);
        return wrapper;
    }

    // --- SCREEN 2: REGISTER & AUDIT PANEL ---
    private JPanel buildRegisterPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(APP_BG);

        RoundedCardPanel card = new RoundedCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 480));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Register & Audit");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField newIdField = createModernTextField("Choose ID");
        JPasswordField newPassField = (JPasswordField) createModernTextField("Password");

        JLabel strengthLabel = new JLabel("Password Strength: Weak");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        strengthLabel.setForeground(new Color(220, 53, 69));
        strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        newPassField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { evaluate(); }
            public void removeUpdate(DocumentEvent e) { evaluate(); }
            public void changedUpdate(DocumentEvent e) { evaluate(); }
            private void evaluate() {
                String pass = new String(newPassField.getPassword());
                int score = 0;
                if (pass.length() >= 8) score += 25;
                if (pass.matches(".*[A-Z].*")) score += 25;
                if (pass.matches(".*[a-z].*")) score += 25;
                if (pass.matches(".*\\d.*")) score += 25;
                
                if (score < 50 || pass.isEmpty()) {
                    strengthLabel.setText("Password Strength: Weak (High Risk)");
                    strengthLabel.setForeground(new Color(220, 53, 69));
                } else if (score < 100) {
                    strengthLabel.setText("Password Strength: Moderate");
                    strengthLabel.setForeground(new Color(253, 126, 20)); 
                } else {
                    strengthLabel.setText("Password Strength: Strong (Secure)");
                    strengthLabel.setForeground(new Color(40, 167, 69)); 
                }
            }
        });

        ModernButton registerBtn = new ModernButton("Create Account", PRIMARY_BLUE, Color.WHITE, PRIMARY_HOVER);
        ModernButton backBtn = new ModernButton("Back to Login", SECONDARY_BTN, TEXT_DARK, SECONDARY_HOVER);

        registerBtn.addActionListener(e -> {
            String id = newIdField.getText().trim();
            String pass = new String(newPassField.getPassword());
            if (id.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
                return;
            }
            userDatabase.put(id, pass);
            JOptionPane.showMessageDialog(this, "Registration & Audit Successful!");
            newIdField.setText("");
            newPassField.setText("");
            strengthLabel.setText("Password Strength: Weak");
            strengthLabel.setForeground(new Color(220, 53, 69));
            cardLayout.show(mainPanel, "LOGIN");
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        card.add(title);
        card.add(Box.createVerticalStrut(35));
        card.add(createLabel("Choose User ID"));
        card.add(newIdField);
        card.add(Box.createVerticalStrut(15));
        card.add(createLabel("Set Secure Password"));
        card.add(newPassField);
        card.add(Box.createVerticalStrut(5));
        card.add(strengthLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(backBtn);

        wrapper.add(card);
        return wrapper;
    }

    // --- SCREEN 3: EDUCATIONAL CRACKER PANEL WITH BACKTRACKING ---
    private JPanel buildCrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(APP_BG);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Header
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBackground(APP_BG);
        JLabel title = new JLabel("Authentication Bypass Visualizer", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);
        
        modeLabel = new JLabel("Mode: INITIALIZING...", SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modeLabel.setForeground(PRIMARY_BLUE);
        
        topPanel.add(title);
        topPanel.add(modeLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center UI
        RoundedCardPanel centerPanel = new RoundedCardPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        displayLabel = new JLabel("----------------", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Consolas", Font.BOLD, 48));
        displayLabel.setForeground(TEXT_DARK);
        displayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        targetHashLabel = new JLabel("Target Hash (DB): Awaiting data...");
        targetHashLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        targetHashLabel.setForeground(TEXT_MUTED);
        targetHashLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        currentHashLabel = new JLabel("Current Guess Hash: Awaiting execution...");
        currentHashLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
        currentHashLabel.setForeground(new Color(220, 53, 69));
        currentHashLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(displayLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(targetHashLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(currentHashLabel);
        
        panel.add(centerPanel, BorderLayout.CENTER);

        // Footer Data
        RoundedCardPanel bottomPanel = new RoundedCardPanel();
        bottomPanel.setLayout(new GridLayout(5, 1, 5, 5));
        bottomPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        attemptsLabel = new JLabel("Total Cryptographic Attempts: 0");
        attemptsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        backtracksLabel = new JLabel("Algorithmic Backtracks (DFS Dead Ends): 0");
        backtracksLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backtracksLabel.setForeground(new Color(220, 53, 69));
        
        statsLabel = new JLabel("Keyspace Info: Calculating...");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(TEXT_MUTED);
        
        statusLabel = new JLabel("Status: Preparing brute-force algorithms...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(PRIMARY_BLUE);
        
        proceedButton = new ModernButton("Proceed to Dashboard ->", PRIMARY_BLUE, Color.WHITE, PRIMARY_HOVER);
        proceedButton.setVisible(false);
        proceedButton.addActionListener(e -> {
            currentHashLabel.setForeground(new Color(220, 53, 69));
            cardLayout.show(mainPanel, "DASHBOARD");
        });
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(proceedButton);
        
        bottomPanel.add(attemptsLabel);
        bottomPanel.add(backtracksLabel);
        bottomPanel.add(statsLabel);
        bottomPanel.add(statusLabel);
        bottomPanel.add(btnWrapper);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void prepareCrackerUI() {
        currentDisplay = new char[currentTargetPassword.length()];
        Arrays.fill(currentDisplay, '-');
        displayLabel.setText(new String(currentDisplay));
        targetHashLabel.setText("Target Hash (DB): " + targetHash);
        currentHashLabel.setText("Current Guess Hash: [GENERATING...]");
        
        int keyspaceSize = SCRAMBLE_CHARS.length();
        int passLength = currentTargetPassword.length();
        double totalCombinations = Math.pow(keyspaceSize, passLength);
        
        statsLabel.setText(String.format("Keyspace: %d chars | Password Length: %d | Total Possible Combinations: %.2E", 
                keyspaceSize, passLength, totalCombinations));
                
        statusLabel.setText("Status: Scanning records...");
        
        attemptsCount = 0;
        backtrackCount = 0;
        attemptsLabel.setText("Total Cryptographic Attempts: 0");
        backtracksLabel.setText("Algorithmic Backtracks (DFS Dead Ends): 0");
        proceedButton.setVisible(false); 
    }

    private void startSpinAnimation() {
        if (timer != null && timer.isRunning()) return;
        lockedIndex = 0;
        animationCycle = 0;
        Arrays.fill(currentDisplay, '-');

        timer = new Timer(40, e -> {
            if (animationCycle == 0) modeLabel.setText("Mode: DICTIONARY ATTACK");
            else if (animationCycle == 20) modeLabel.setText("Mode: RECURSIVE BRUTE FORCE (DFS)");

            for (int i = lockedIndex; i < currentTargetPassword.length(); i++) {
                currentDisplay[i] = SCRAMBLE_CHARS.charAt(random.nextInt(SCRAMBLE_CHARS.length()));
            }

            String currentGuess = new String(currentDisplay);
            displayLabel.setText(currentGuess);
            currentHashLabel.setText("Current Guess Hash: " + generateMockHash(currentGuess));
            
            attemptsCount += random.nextInt(450000) + 50000;
            backtrackCount += random.nextInt(15000) + 2000; 
            
            attemptsLabel.setText("Total Cryptographic Attempts: " + formatter.format(attemptsCount));
            backtracksLabel.setText("Algorithmic Backtracks (DFS Dead Ends): " + formatter.format(backtrackCount));
            
            animationCycle++;

            if (animationCycle % 8 == 0) {
                currentDisplay[lockedIndex] = currentTargetPassword.charAt(lockedIndex);
                lockedIndex++;
                playClickSound(); 
            }

            if (lockedIndex >= currentTargetPassword.length()) {
                timer.stop();
                displayLabel.setText(currentTargetPassword);
                currentHashLabel.setText("Current Guess Hash: " + targetHash); 
                currentHashLabel.setForeground(new Color(40, 167, 69)); 
                
                statusLabel.setText("Status: HASH COLLISION FOUND. Password Recovered.");
                statusLabel.setForeground(new Color(40, 167, 69)); 
                modeLabel.setText("Mode: AUDIT COMPLETE");
                
                proceedButton.setVisible(true);
            }
        });
        timer.start();
    }
    
    private String generateMockHash(String input) {
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            for (int i = 0; i < 16; i++) { 
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
        } catch(Exception e) { return "ERROR"; }
        return hexString.toString() + "...";
    }

    // --- SCREEN 4: DASHBOARD ---
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(APP_BG);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel dashTitle = new JLabel("User Dashboard");
        dashTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dashTitle.setForeground(TEXT_DARK);
        headerPanel.add(dashTitle, BorderLayout.WEST);

        ModernButton logoutBtn = new ModernButton("Logout", SECONDARY_BTN, TEXT_DARK, SECONDARY_HOVER);
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 60));
        contentPanel.setOpaque(false);

        contentPanel.add(createAppIcon("📁", "Documents", e -> openDocumentsApp()));
        contentPanel.add(createAppIcon("🖼️", "Pictures", e -> openPicturesApp()));
        contentPanel.add(createAppIcon("⚙️", "Settings", e -> openSettingsApp()));

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAppIcon(String emoji, String title, ActionListener action) {
        RoundedCardPanel card = new RoundedCardPanel();
        card.setPreferredSize(new Dimension(160, 160));
        card.setLayout(new BorderLayout());
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        card.add(icon, BorderLayout.CENTER);
        
        JLabel text = new JLabel(title, SwingConstants.CENTER);
        text.setFont(new Font("Segoe UI", Font.BOLD, 14));
        text.setForeground(TEXT_DARK);
        text.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(text, BorderLayout.SOUTH);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null)); }
            @Override
            public void mouseEntered(MouseEvent e) { card.setBackground(SECONDARY_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });
        return card;
    }

    // Apps (Docs, Pics, Settings)
    private void openDocumentsApp() {
        JDialog d = new JDialog(this, "Text Editor", true);
        d.setSize(450, 350);
        d.setLocationRelativeTo(this);
        JTextArea area = new JTextArea("Welcome to your personal notes...\n\n- Parking Slot 14 is reserved.\n- Renew monthly pass next week.");
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(new EmptyBorder(15, 15, 15, 15));
        d.add(new JScrollPane(area), BorderLayout.CENTER);
        d.setVisible(true);
    }

    private void openPicturesApp() {
        JDialog d = new JDialog(this, "Gallery Viewer", true);
        d.setSize(500, 400);
        d.setLocationRelativeTo(this);
        JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15));
        grid.setBorder(new EmptyBorder(15, 15, 15, 15));
        grid.setBackground(APP_BG);
        grid.add(createMockImagePanel(new Color(208, 235, 255), "🛫", "Holiday.png"));
        grid.add(createMockImagePanel(new Color(211, 249, 216), "🚗", "Car.png"));
        grid.add(createMockImagePanel(new Color(255, 227, 227), "👨‍👩‍👧", "Family.jpg"));
        grid.add(createMockImagePanel(new Color(255, 240, 207), "🐶", "Dog.jpg"));
        d.add(grid, BorderLayout.CENTER);
        d.setVisible(true);
    }
    
    private JPanel createMockImagePanel(Color color, String emoji, String filename) {
        RoundedCardPanel pnl = new RoundedCardPanel();
        pnl.setBackground(color);
        pnl.setLayout(new BorderLayout());
        JLabel el = new JLabel(emoji, SwingConstants.CENTER);
        el.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50)); 
        pnl.add(el, BorderLayout.CENTER);
        JLabel nl = new JLabel(filename, SwingConstants.CENTER);
        nl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nl.setForeground(new Color(0, 0, 0, 150));
        nl.setBorder(new EmptyBorder(0, 0, 10, 0)); 
        pnl.add(nl, BorderLayout.SOUTH);
        return pnl;
    }

    private void openSettingsApp() {
        JDialog d = new JDialog(this, "Settings", true);
        d.setSize(350, 250);
        d.setLocationRelativeTo(this);
        JPanel pnl = new JPanel(new GridLayout(3, 1, 10, 10));
        pnl.setBorder(new EmptyBorder(25, 25, 25, 25));
        pnl.setBackground(Color.WHITE);
        JCheckBox c1 = new JCheckBox("Enable Email Notifications", true);
        JCheckBox c2 = new JCheckBox("Share Location for Parking", true);
        JCheckBox c3 = new JCheckBox("Auto-renew Pass", false);
        c1.setBackground(Color.WHITE); c2.setBackground(Color.WHITE); c3.setBackground(Color.WHITE);
        pnl.add(c1); pnl.add(c2); pnl.add(c3);
        d.add(pnl, BorderLayout.CENTER);
        d.setVisible(true);
    }

    // --- CUSTOM UI COMPONENTS ---

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField createModernTextField(String type) {
        JTextField field = type.equals("Password") ? new JPasswordField() : new JTextField();
        field.setMaximumSize(new Dimension(300, 40));
        field.setPreferredSize(new Dimension(300, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    // Custom Rounded Button Class
    class ModernButton extends JButton {
        private Color bg, hover;
        public ModernButton(String text, Color bg, Color fg, Color hover) {
            super(text);
            this.bg = bg; this.hover = hover;
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(300, 40));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(hover); }
                public void mouseExited(MouseEvent e) { setBackground(bg); }
            });
            setBackground(bg);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // Custom Rounded Panel Class (For Cards)
    class RoundedCardPanel extends JPanel {
        public RoundedCardPanel() { setOpaque(false); setBackground(Color.WHITE); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginSimulatorApp().setVisible(true));
    }
}