import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame — the first screen users see.
 * Clean dark-mode login with Register dialog and show/hide password.
 */
public class LoginFrame extends JFrame {

    // ── Colour palette ───────────────────────────────────────────────
    private static final Color BG_TOP   = new Color(10,  18,  36);
    private static final Color BG_BOT   = new Color(20,  32,  54);
    private static final Color CARD_BG  = new Color(28,  40,  62);
    private static final Color BORDER   = new Color(56, 189, 248, 80);
    private static final Color ACCENT   = new Color(56, 189, 248);
    private static final Color ACCENT2  = new Color(99, 102, 241);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color MUTED    = new Color(100, 116, 139);
    private static final Color ERR      = new Color(248, 113, 113);
    private static final Color OK       = new Color( 74, 222, 128);
    private static final Color FIELD_BG = new Color(13,  21,  39);

    private final UserManager userManager;

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JCheckBox      showPwCheck;
    private JLabel         statusLabel;

    public LoginFrame() {
        this.userManager = new UserManager();
        buildUI();
    }

    // ── Build UI ─────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Bus Ticket Booking System -- Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 510);
        setLocationRelativeTo(null);
        setResizable(false);

        // Gradient background
        GradientPanel bg = new GradientPanel(BG_TOP, BG_BOT);
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // ── Card ─────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundLineBorder(BORDER, 16, 1.5f),
            BorderFactory.createEmptyBorder(30, 36, 30, 36)
        ));
        card.setPreferredSize(new Dimension(360, 420));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 0, 0);

        // Title block
        gc.gridy = 0; gc.insets = new Insets(0, 0, 4, 0);
        JLabel titleIcon = new JLabel("BUS", SwingConstants.CENTER);
        titleIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleIcon.setForeground(CARD_BG);
        titleIcon.setBackground(ACCENT);
        titleIcon.setOpaque(true);
        titleIcon.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconWrap.setOpaque(false);
        iconWrap.add(titleIcon);
        card.add(iconWrap, gc);

        gc.gridy = 1; gc.insets = new Insets(10, 0, 2, 0);
        JLabel title = centeredLabel("Bus Ticket Booking", 20, Font.BOLD, ACCENT);
        card.add(title, gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 26, 0);
        card.add(centeredLabel("CSE 110 Project", 12, Font.PLAIN, MUTED), gc);

        // Username
        gc.gridy = 3; gc.insets = new Insets(0, 0, 5, 0);
        card.add(fieldLabel("Username"), gc);

        gc.gridy = 4; gc.insets = new Insets(0, 0, 14, 0);
        usernameField = new JTextField();
        styleField(usernameField);
        card.add(usernameField, gc);

        // Password
        gc.gridy = 5; gc.insets = new Insets(0, 0, 5, 0);
        card.add(fieldLabel("Password"), gc);

        gc.gridy = 6; gc.insets = new Insets(0, 0, 6, 0);
        passwordField = new JPasswordField();
        styleField(passwordField);
        card.add(passwordField, gc);

        // Show password checkbox
        gc.gridy = 7; gc.insets = new Insets(0, 2, 18, 0);
        showPwCheck = new JCheckBox("Show password");
        showPwCheck.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        showPwCheck.setForeground(MUTED);
        showPwCheck.setBackground(CARD_BG);
        showPwCheck.setFocusPainted(false);
        showPwCheck.addActionListener(e ->
            passwordField.setEchoChar(showPwCheck.isSelected() ? (char) 0 : '*'));
        card.add(showPwCheck, gc);

        // Status label
        gc.gridy = 8; gc.insets = new Insets(0, 0, 12, 0);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ERR);
        card.add(statusLabel, gc);

        // Buttons
        gc.gridy = 9; gc.insets = new Insets(0, 0, 0, 0);
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);

        JButton loginBtn = makeButton("Login",    ACCENT2, Color.WHITE);
        JButton regBtn   = makeButton("Register", new Color(44, 58, 80), TEXT);

        loginBtn.addActionListener(e -> doLogin());
        regBtn  .addActionListener(e -> doRegister());
        getRootPane().setDefaultButton(loginBtn);

        btnRow.add(loginBtn);
        btnRow.add(regBtn);
        card.add(btnRow, gc);

        bg.add(card);
        setVisible(true);
    }

    // ── Actions ──────────────────────────────────────────────────────

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            setStatus("Please fill in all fields.", false);
            return;
        }

        String loggedIn = userManager.login(user, pass);
        if (loggedIn != null) {
            setStatus("Welcome, " + loggedIn + "!", true);
            Timer t = new Timer(600, e -> { dispose(); new MainFrame(loggedIn); });
            t.setRepeats(false);
            t.start();
        } else {
            setStatus("Invalid username or password.", false);
            passwordField.setText("");
        }
    }

    private void doRegister() {
        JDialog dlg = new JDialog(this, "Create Account", true);
        dlg.setSize(340, 290);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(28, 40, 62));
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 5, 0);

        JTextField  newUser = new JTextField();
        JPasswordField newPw  = new JPasswordField();
        JPasswordField newPw2 = new JPasswordField();
        styleField(newUser); styleField(newPw); styleField(newPw2);

        gc.gridy = 0; p.add(fieldLabel("Username"), gc);
        gc.gridy = 1; gc.insets = new Insets(0,0,12,0); p.add(newUser, gc);
        gc.gridy = 2; gc.insets = new Insets(0,0,5,0);  p.add(fieldLabel("Password (min 4 chars)"), gc);
        gc.gridy = 3; gc.insets = new Insets(0,0,12,0); p.add(newPw, gc);
        gc.gridy = 4; gc.insets = new Insets(0,0,5,0);  p.add(fieldLabel("Confirm Password"), gc);
        gc.gridy = 5; gc.insets = new Insets(0,0,10,0); p.add(newPw2, gc);

        JLabel dlgStatus = new JLabel(" ");
        dlgStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dlgStatus.setForeground(ERR);
        gc.gridy = 6; gc.insets = new Insets(0,0,8,0); p.add(dlgStatus, gc);

        JButton createBtn = makeButton("Create Account", ACCENT2, Color.WHITE);
        gc.gridy = 7; gc.insets = new Insets(0,0,0,0);
        createBtn.addActionListener(e -> {
            String u   = newUser.getText().trim();
            String pw  = new String(newPw.getPassword());
            String pw2 = new String(newPw2.getPassword());
            if (u.isEmpty() || pw.isEmpty())      { dlgStatus.setText("Fields cannot be empty."); return; }
            if (!pw.equals(pw2))                   { dlgStatus.setText("Passwords do not match."); return; }
            if (pw.length() < 4)                   { dlgStatus.setText("Password must be at least 4 characters."); return; }
            if (userManager.register(u, pw)) {
                JOptionPane.showMessageDialog(dlg, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            } else {
                dlgStatus.setText("Username already taken.");
            }
        });
        p.add(createBtn, gc);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ok ? OK : ERR);
    }

    // ── UI Helpers ───────────────────────────────────────────────────

    private JLabel centeredLabel(String text, int size, int style, Color fg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(fg);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(FIELD_BG);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        f.setPreferredSize(new Dimension(288, 36));
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker()
                          : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 38));
        return b;
    }

    // ── Inner classes ────────────────────────────────────────────────

    private static class GradientPanel extends JPanel {
        private final Color top, bot;
        GradientPanel(Color top, Color bot) { this.top = top; this.bot = bot; setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ((Graphics2D) g).setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class RoundLineBorder extends AbstractBorder {
        private final Color color; private final int arc; private final float stroke;
        RoundLineBorder(Color c, int arc, float stroke) { this.color=c; this.arc=arc; this.stroke=stroke; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(stroke));
            g2.drawRoundRect(x, y, w-1, h-1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(0,0,0,0); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}
