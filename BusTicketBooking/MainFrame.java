import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * MainFrame — main application window shown after login.
 * Tabs: View Buses | Book Ticket | My Tickets | Admin Panel
 */
public class MainFrame extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────
    private static final Color BG        = new Color(13,  21,  40);
    private static final Color SURFACE   = new Color(28,  40,  62);
    private static final Color SURFACE2  = new Color(44,  58,  80);
    private static final Color ACCENT    = new Color(56, 189, 248);
    private static final Color ACCENT2   = new Color(99, 102, 241);
    private static final Color SUCCESS   = new Color(74, 222, 128);
    private static final Color DANGER    = new Color(248, 113, 113);
    private static final Color TEXT      = new Color(226, 232, 240);
    private static final Color TEXT_MUTED= new Color(100, 116, 139);
    private static final Color FIELD_BG  = new Color(13,  21,  40);
    private static final Color ROW_EVEN  = new Color(22,  34,  56);
    private static final Color ROW_ODD   = new Color(28,  40,  62);
    private static final Color ROW_SEL   = new Color(56, 189, 248, 60);

    private static final String TICKET_FILE = "tickets.txt";
    private static final Font BODY   = new Font("Segoe UI", Font.PLAIN,  13);
    private static final Font BOLD   = new Font("Segoe UI", Font.BOLD,   13);
    private static final Font HEAD18 = new Font("Segoe UI", Font.BOLD,   18);

    private final String currentUser;
    private final List<Bus>    buses   = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private int ticketCounter = 1;

    // Buses tab
    private DefaultTableModel busModel;

    // Tickets tab
    private DefaultTableModel ticketModel;
    private JTable ticketTable;

    // Book tab
    private JComboBox<String> busCombo;
    private JTextField bookNameField;
    private JLabel bookStatus;

    public MainFrame(String currentUser) {
        this.currentUser = currentUser;
        initBuses();
        loadTickets();
        buildUI();
    }

    // ── Top-level layout ─────────────────────────────────────────────

    private void buildUI() {
        setTitle("Bus Ticket Booking System  —  Welcome, " + currentUser);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setBackground(BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        setContentPane(root);
        setVisible(true);
    }

    // ── Header ───────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SURFACE);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(56, 189, 248, 60)));
        h.setPreferredSize(new Dimension(0, 54));

        JLabel logo = new JLabel("   Bus Ticket Booking System");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(ACCENT);
        h.add(logo, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        right.setOpaque(false);

        JLabel user = new JLabel("User: " + currentUser);
        user.setFont(BODY);
        user.setForeground(TEXT);
        right.add(user);

        JButton logout = pillBtn("Logout", SURFACE2, TEXT_MUTED, 80, 32);
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        right.add(logout);

        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ── Tabs ─────────────────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(BOLD);
        tabs.setBackground(SURFACE);
        tabs.setForeground(TEXT);

        tabs.addTab("  View Buses  ",   buildBusesTab());
        tabs.addTab("  Book Ticket  ",  buildBookTab());
        tabs.addTab("  My Tickets  ",   buildMyTicketsTab());
        if (UserManager.isAdmin(currentUser)) {
            tabs.addTab("  Admin Panel  ", buildAdminTab());
        }

        tabs.addChangeListener(e -> {
            String title = tabs.getTitleAt(tabs.getSelectedIndex());
            if (title.contains("View Buses"))  refreshBusTable();
            if (title.contains("My Tickets"))  refreshTicketTable(null);
            if (title.contains("Book Ticket")) refreshCombo();
        });

        return tabs;
    }

    // ── TAB 1 — View Buses ───────────────────────────────────────────

    private JPanel buildBusesTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        JLabel heading = new JLabel("Available Buses");
        heading.setFont(HEAD18);
        heading.setForeground(ACCENT);
        p.add(heading, BorderLayout.NORTH);

        busModel = new DefaultTableModel(
            new String[]{"Bus ID","Operator","Route","Seats Left","Total Seats","Price (BDT)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(busModel);
        refreshBusTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(56,189,248,40)));
        p.add(scroll, BorderLayout.CENTER);

        JButton refresh = pillBtn("Refresh", SURFACE2, TEXT, 100, 32);
        refresh.addActionListener(e -> refreshBusTable());
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bot.setOpaque(false);
        bot.add(refresh);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private void refreshBusTable() {
        if (busModel == null) return;
        busModel.setRowCount(0);
        for (Bus b : buses) {
            busModel.addRow(new Object[]{
                b.getBusId(), b.getOperatorName(), b.getRoute(),
                b.getAvailableSeats(), b.getTotalSeats(),
                String.format("%.0f", b.getTicketPrice())
            });
        }
    }

    // ── TAB 2 — Book Ticket ──────────────────────────────────────────

    private JPanel buildBookTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56, 189, 248, 50), 1),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));
        card.setPreferredSize(new Dimension(440, 330));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        // Heading
        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0);
        JLabel h = new JLabel("Book a Ticket");
        h.setFont(HEAD18); h.setForeground(ACCENT);
        card.add(h, gc);

        // Bus selector
        gc.gridy = 1; gc.insets = new Insets(0, 0, 6, 0);
        card.add(formLbl("Select Bus:"), gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 16, 0);
        busCombo = new JComboBox<>();
        busCombo.setFont(BODY);
        busCombo.setBackground(FIELD_BG);
        busCombo.setForeground(TEXT);
        busCombo.setPreferredSize(new Dimension(376, 34));
        refreshCombo();
        card.add(busCombo, gc);

        // Passenger name
        gc.gridy = 3; gc.insets = new Insets(0, 0, 6, 0);
        card.add(formLbl("Passenger Name:"), gc);

        gc.gridy = 4; gc.insets = new Insets(0, 0, 18, 0);
        bookNameField = new JTextField();
        styleField(bookNameField);
        card.add(bookNameField, gc);

        // Status
        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        bookStatus = new JLabel(" ");
        bookStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookStatus.setForeground(DANGER);
        card.add(bookStatus, gc);

        // Button
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        JButton book = pillBtn("Confirm Booking", ACCENT2, Color.WHITE, 180, 38);
        book.addActionListener(e -> doBookTicket());
        card.add(book, gc);

        outer.add(card);
        return outer;
    }

    private void refreshCombo() {
        if (busCombo == null) return;
        busCombo.removeAllItems();
        for (Bus b : buses) {
            busCombo.addItem(b.getBusId() + "  |  " + b.getOperatorName()
                + "  |  " + b.getRoute()
                + "  |  Seats left: " + b.getAvailableSeats()
                + "  |  " + (int)b.getTicketPrice() + " BDT");
        }
    }

    private void doBookTicket() {
        int idx = busCombo.getSelectedIndex();
        if (idx < 0) { setBookStatus("Please select a bus.", false); return; }
        String name = bookNameField.getText().trim();
        if (name.isEmpty()) { setBookStatus("Passenger name is required.", false); return; }

        Bus selected = buses.get(idx);
        try {
            if (!selected.bookSeat()) throw new SeatNotAvailableException(selected.getBusId());
            int seat = selected.getTotalSeats() - selected.getAvailableSeats();
            String id = "TK-" + ticketCounter++;
            Ticket t  = new Ticket(id, name, selected.getBusId(),
                                   selected.getRoute(), seat, selected.getTicketPrice());
            tickets.add(t);
            saveTickets();
            setBookStatus("Ticket " + id + " booked! Seat #" + seat, true);
            bookNameField.setText("");
            refreshCombo();
            refreshBusTable();
        } catch (SeatNotAvailableException ex) {
            setBookStatus("Error: " + ex.getMessage(), false);
        }
    }

    private void setBookStatus(String msg, boolean ok) {
        bookStatus.setText(msg);
        bookStatus.setForeground(ok ? SUCCESS : DANGER);
    }

    // ── TAB 3 — My Tickets ───────────────────────────────────────────

    private JPanel buildMyTicketsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 16, 20));

        // Top row: title + search
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JLabel heading = new JLabel("My Tickets");
        heading.setFont(HEAD18);
        heading.setForeground(ACCENT);
        topRow.add(heading, BorderLayout.WEST);

        JPanel searchArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchArea.setOpaque(false);
        JLabel lbl = new JLabel("Search by name:");
        lbl.setFont(BODY); lbl.setForeground(TEXT_MUTED);
        JTextField searchField = new JTextField(14);
        styleField(searchField);
        searchField.setPreferredSize(new Dimension(160, 32));

        JButton searchBtn = pillBtn("Search",   ACCENT2,  Color.WHITE, 80, 32);
        JButton clearBtn  = pillBtn("Show All", SURFACE2, TEXT,        80, 32);
        searchBtn.addActionListener(e -> refreshTicketTable(searchField.getText().trim()));
        clearBtn .addActionListener(e -> { searchField.setText(""); refreshTicketTable(null); });

        searchArea.add(lbl);
        searchArea.add(searchField);
        searchArea.add(searchBtn);
        searchArea.add(clearBtn);
        topRow.add(searchArea, BorderLayout.EAST);
        p.add(topRow, BorderLayout.NORTH);

        // Table
        ticketModel = new DefaultTableModel(
            new String[]{"Ticket ID","Passenger","Bus ID","Route","Seat","Fare (BDT)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ticketTable = styledTable(ticketModel);
        refreshTicketTable(null);

        JScrollPane scroll = new JScrollPane(ticketTable);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(56,189,248,40)));
        p.add(scroll, BorderLayout.CENTER);

        // Cancel button
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bot.setOpaque(false);
        JButton cancelBtn = pillBtn("Cancel Selected Ticket", DANGER, Color.WHITE, 200, 34);
        cancelBtn.addActionListener(e -> doCancelTicket());
        bot.add(cancelBtn);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private void refreshTicketTable(String filter) {
        if (ticketModel == null) return;
        ticketModel.setRowCount(0);
        for (Ticket t : tickets) {
            if (filter != null && !filter.isEmpty()
                    && !t.getPassengerName().toLowerCase().contains(filter.toLowerCase())) continue;
            ticketModel.addRow(new Object[]{
                t.getTicketId(), t.getPassengerName(), t.getBusId(),
                t.getRoute(), t.getSeatNumber(), String.format("%.0f", t.getFare())
            });
        }
    }

    private void doCancelTicket() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket row first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) ticketModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Cancel ticket " + id + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tickets.removeIf(t -> {
                if (t.getTicketId().equalsIgnoreCase(id)) {
                    Bus b = findBus(t.getBusId());
                    if (b != null) b.cancelSeat();
                    return true;
                }
                return false;
            });
            saveTickets();
            refreshTicketTable(null);
            refreshBusTable();
            refreshCombo();
            JOptionPane.showMessageDialog(this, "Ticket " + id + " cancelled.",
                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── TAB 4 — Admin Panel ──────────────────────────────────────────

    private JPanel buildAdminTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 20));
        outer.setBackground(BG);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Admin Panel  -  Add New Bus");
        heading.setFont(HEAD18);
        heading.setForeground(ACCENT);
        outer.add(heading, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56,189,248,50), 1),
            BorderFactory.createEmptyBorder(22, 28, 22, 28)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Bus ID (e.g. B105):", "Operator Name:", "Route (From -> To):",
                           "Total Seats:", "Ticket Price (BDT):"};
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new JTextField(18);
            styleField(fields[i]);

            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(BOLD); lbl.setForeground(TEXT);
            card.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 1;
            card.add(fields[i], gc);
        }

        JLabel adminStatus = new JLabel(" ");
        adminStatus.setFont(BODY); adminStatus.setForeground(DANGER);
        gc.gridx = 0; gc.gridy = labels.length; gc.gridwidth = 2; gc.weightx = 1;
        card.add(adminStatus, gc);

        JButton addBtn = pillBtn("Add Bus", SUCCESS, new Color(13,21,40), 130, 36);
        gc.gridy = labels.length + 1; gc.gridwidth = 1; gc.weightx = 0;
        addBtn.addActionListener(e -> {
            String id  = fields[0].getText().trim().toUpperCase();
            String op  = fields[1].getText().trim();
            String rt  = fields[2].getText().trim();
            String st  = fields[3].getText().trim();
            String pr  = fields[4].getText().trim();
            if (id.isEmpty()||op.isEmpty()||rt.isEmpty()||st.isEmpty()||pr.isEmpty()) {
                adminStatus.setForeground(DANGER);
                adminStatus.setText("All fields are required."); return;
            }
            try {
                int seats = Integer.parseInt(st);
                double price = Double.parseDouble(pr);
                if (findBus(id) != null) {
                    adminStatus.setForeground(DANGER);
                    adminStatus.setText("Bus ID already exists."); return;
                }
                buses.add(new Bus(id, op, rt, seats, price));
                adminStatus.setForeground(SUCCESS);
                adminStatus.setText("Bus " + id + " added successfully!");
                for (JTextField f : fields) f.setText("");
                refreshBusTable(); refreshCombo();
            } catch (NumberFormatException ex) {
                adminStatus.setForeground(DANGER);
                adminStatus.setText("Seats and Price must be valid numbers.");
            }
        });
        card.add(addBtn, gc);

        outer.add(card, BorderLayout.CENTER);

        JLabel info = new JLabel("  Total bookings so far: " + tickets.size());
        info.setFont(BODY); info.setForeground(TEXT_MUTED);
        outer.add(info, BorderLayout.SOUTH);

        return outer;
    }

    // ── Data helpers ─────────────────────────────────────────────────

    private void initBuses() {
        buses.add(new Bus("B101", "Green Line",       "Dhaka -> Chittagong",       40, 550));
        buses.add(new Bus("B102", "Shyamoli",         "Dhaka -> Sylhet",           35, 480));
        buses.add(new Bus("B103", "Hanif Enterprise", "Dhaka -> Rajshahi",         45, 420));
        buses.add(new Bus("B104", "S. Alam",          "Chittagong -> Cox's Bazar", 30, 350));
    }

    private Bus findBus(String id) {
        return buses.stream().filter(b -> b.getBusId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    private void saveTickets() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TICKET_FILE))) {
            bw.write("# ticketId,passengerName,busId,route,seatNumber,fare"); bw.newLine();
            for (Ticket t : tickets) { bw.write(t.toFileString()); bw.newLine(); }
        } catch (IOException e) { System.out.println("[Warning] Save failed: " + e.getMessage()); }
    }

    private void loadTickets() {
        File f = new File(TICKET_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.isBlank()) continue;
                try {
                    Ticket t = Ticket.fromFileString(line);
                    tickets.add(t);
                    int n = Integer.parseInt(t.getTicketId().replace("TK-", ""));
                    if (n >= ticketCounter) ticketCounter = n + 1;
                } catch (Exception ignored) {}
            }
        } catch (IOException ignored) {}
    }

    // ── Swing helpers ────────────────────────────────────────────────

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row) ? ROW_SEL : (row%2==0 ? ROW_EVEN : ROW_ODD));
                c.setForeground(TEXT);
                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return c;
            }
        };
        t.setFont(BODY);
        t.setForeground(TEXT);
        t.setBackground(BG);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT);

        JTableHeader hdr = t.getTableHeader();
        hdr.setFont(BOLD);
        hdr.setBackground(SURFACE2);
        hdr.setForeground(ACCENT);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        hdr.setReorderingAllowed(false);
        hdr.setPreferredSize(new Dimension(0, 36));
        return t;
    }

    private void styleField(JTextField f) {
        f.setFont(BODY);
        f.setForeground(TEXT);
        f.setBackground(FIELD_BG);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(56,189,248,80), 1),
            BorderFactory.createEmptyBorder(6, 9, 6, 9)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    private JLabel formLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD); l.setForeground(TEXT);
        return l;
    }

    private JButton pillBtn(String text, Color bg, Color fg, int w, int h) {
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
        b.setFont(BOLD);
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }
}
