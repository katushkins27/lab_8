package client.gui;

import common.auth.Credentials;
import common.data.*;
import common.network.Request;
import common.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainWindow extends JFrame {
    private final LocaleManager lm = LocaleManager.getInstance();
    private final Credentials credentials;
    private final client.NetworkManager network;

    private final TicketTableModel tableModel = new TicketTableModel();
    private TablePanel tablePanel;
    private VisualPanel visualPanel;
    private JTabbedPane tabbedPane;
    private JLabel userLabel;
    private JComboBox<LocaleManager.SupportedLocale> langCombo;
    private JLabel langLabel;
    private JButton btnAdd, btnUpdate, btnRemove, btnRemoveById, btnRemoveGreater,
            btnRemoveHead, btnHead, btnClear, btnRemoveByPrice, btnRemoveByType,
            btnMinByVenue, btnShow, btnInfo, btnHelp, btnScript, btnLogout;
    private final Timer refreshTimer = new Timer(true);
    private boolean refreshing = false;

    private final Map<Integer, String> ownerMap = new HashMap<>();

    public MainWindow(Credentials credentials, client.NetworkManager network) {
        this.credentials = credentials;
        this.network = network;
        buildUI();
        lm.addChangeListener(this::updateAllTexts);
        startAutoRefresh();
        refreshData();
    }

    private void buildUI() {
        setTitle(lm.get("main.title"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(248, 249, 252));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildCommandBar(), BorderLayout.EAST);

        setContentPane(root);
    }
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(45, 55, 75));
        bar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel titleLbl = new JLabel(lm.get("main.title"));
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        langLabel = new JLabel(lm.get("main.lang") + ":");
        langLabel.setForeground(new Color(180, 190, 210));
        langLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        langCombo = new JComboBox<>(LocaleManager.SupportedLocale.values()) {
            @Override public Dimension getPreferredSize() { return new Dimension(140, 26); }
        };
        langCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, idx, sel, foc);
                if (value instanceof LocaleManager.SupportedLocale sl) setText(sl.displayName);
                return this;
            }
        });
        langCombo.setSelectedItem(lm.getCurrentLocale());
        langCombo.addActionListener(e -> {
            if (langCombo.getSelectedItem() instanceof LocaleManager.SupportedLocale sl) {
                lm.setLocale(sl);
            }
        });

        userLabel = new JLabel(lm.get("main.user") + ": " + credentials.getLogin());
        userLabel.setForeground(new Color(160, 220, 160));
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        right.add(userLabel);
        right.add(langLabel);
        right.add(langCombo);

        bar.add(titleLbl, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildCenter() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tablePanel = new TablePanel(tableModel);
        tablePanel.setOnEdit(this::editTicket);
        tablePanel.setOnDelete(this::deleteSelectedTicket);

        visualPanel = new VisualPanel();
        visualPanel.setOnTicketClick((ticket, point) -> showTicketInfo(ticket));
        visualPanel.setOnTicketDoubleClick(this::editTicket);

        tabbedPane.addTab(lm.get("main.tab.table"), tablePanel);
        tabbedPane.addTab(lm.get("main.tab.visual"), visualPanel);

        return tabbedPane;
    }
    private JPanel buildCommandBar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setBackground(new Color(250, 251, 255));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(210, 215, 228)),
                new EmptyBorder(10, 10, 10, 10)));
        bar.setPreferredSize(new Dimension(175, 0));

        btnAdd          = cmdBtn(lm.get("cmd.add"),             new Color(66, 135, 245));
        btnUpdate       = cmdBtn(lm.get("cmd.update"),          new Color(80, 160, 80));
        btnRemove       = cmdBtn(lm.get("cmd.remove"),          new Color(210, 70, 70));
        btnRemoveById   = cmdBtn(lm.get("cmd.remove_by_id"),    new Color(200, 90, 50));
        btnRemoveGreater= cmdBtn(lm.get("cmd.remove_greater"),  new Color(170, 80, 180));
        btnRemoveHead   = cmdBtn(lm.get("cmd.remove_head"),     new Color(180, 120, 50));
        btnHead         = cmdBtn(lm.get("cmd.head"),            new Color(100, 140, 200));
        btnClear        = cmdBtn(lm.get("cmd.clear"),           new Color(190, 50, 50));
        btnRemoveByPrice= cmdBtn(lm.get("cmd.remove_all_by_price"), new Color(180, 100, 60));
        btnRemoveByType = cmdBtn(lm.get("cmd.remove_any_by_type"),  new Color(130, 90, 190));
        btnMinByVenue   = cmdBtn(lm.get("cmd.min_by_venue"),    new Color(60, 160, 170));
        btnShow         = cmdBtn(lm.get("cmd.show"),            new Color(90, 130, 200));
        btnInfo         = cmdBtn(lm.get("cmd.info"),            new Color(120, 130, 145));
        btnHelp         = cmdBtn(lm.get("cmd.help"),            new Color(120, 130, 145));
        btnScript       = cmdBtn(lm.get("cmd.execute_script"),  new Color(100, 115, 140));
        btnLogout       = cmdBtn(lm.get("cmd.logout"),          new Color(90, 95, 110));

        JButton[] buttons = {btnAdd, btnUpdate, btnRemove, btnRemoveById, btnRemoveGreater,
                btnRemoveHead, btnHead, btnClear, btnRemoveByPrice, btnRemoveByType,
                btnMinByVenue, null, btnShow, btnInfo, btnHelp, btnScript, null, btnLogout};

        for (JButton btn : buttons) {
            if (btn == null) {
                bar.add(Box.createVerticalStrut(6));
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                bar.add(sep);
                bar.add(Box.createVerticalStrut(6));
            } else {
                bar.add(btn);
                bar.add(Box.createVerticalStrut(4));
            }
        }

        btnAdd.addActionListener(e -> cmdAdd());
        btnUpdate.addActionListener(e -> cmdUpdate());
        btnRemove.addActionListener(e -> cmdRemoveSelected());
        btnRemoveById.addActionListener(e -> cmdRemoveById());
        btnRemoveGreater.addActionListener(e -> cmdRemoveGreater());
        btnRemoveHead.addActionListener(e -> cmdSimple("remove_head"));
        btnHead.addActionListener(e -> cmdSimple("head"));
        btnClear.addActionListener(e -> cmdClear());
        btnRemoveByPrice.addActionListener(e -> cmdRemoveByPrice());
        btnRemoveByType.addActionListener(e -> cmdRemoveByType());
        btnMinByVenue.addActionListener(e -> cmdSimple("min_by_venue"));
        btnShow.addActionListener(e -> refreshData());
        btnInfo.addActionListener(e -> cmdSimple("info"));
        btnHelp.addActionListener(e -> cmdSimple("help"));
        btnScript.addActionListener(e -> cmdScript());
        btnLogout.addActionListener(e -> logout());

        return bar;
    }

    private JButton cmdBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
    private void cmdAdd() {
        TicketDialog dlg = new TicketDialog(this, false);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        Ticket t = dlg.buildTicket();
        sendRequest(new Request("add", t, credentials), true);
    }

    private void cmdUpdate() {
        Ticket sel = tablePanel.getSelectedTicket();
        if (sel == null) { showMsg(lm.get("msg.not_selected")); return; }
        if (!isOwner(sel)) { showMsg(lm.get("msg.only_owner")); return; }
        editTicket(sel);
    }

    private void editTicket(Ticket t) {
        if (!isOwner(t)) { showMsg(lm.get("msg.only_owner")); return; }
        TicketDialog dlg = new TicketDialog(this, true);
        dlg.fillFrom(t);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        Ticket updated = dlg.buildTicket();
        String idArg = String.valueOf(t.getId());
        sendRequest(new Request("update", idArg, updated, credentials), true);
    }

    private void cmdRemoveSelected() {
        Ticket sel = tablePanel.getSelectedTicket();
        if (sel == null) { showMsg(lm.get("msg.not_selected")); return; }
        deleteSelectedTicket(sel);
    }

    private void deleteSelectedTicket(Ticket t) {
        if (!isOwner(t)) { showMsg(lm.get("msg.only_owner")); return; }
        int res = JOptionPane.showConfirmDialog(this,
                lm.get("dialog.confirm.remove"), lm.get("dialog.confirm"), JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        sendRequest(new Request("remove_by_id", String.valueOf(t.getId()), credentials), true);
    }

    private void cmdRemoveById() {
        String id = JOptionPane.showInputDialog(this, lm.get("dialog.remove_by_id"));
        if (id == null || id.isBlank()) return;
        sendRequest(new Request("remove_by_id", id.trim(), credentials), true);
    }

    private void cmdRemoveGreater() {
        TicketDialog dlg = new TicketDialog(this, false);
        dlg.setTitle(lm.get("cmd.remove_greater"));
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        sendRequest(new Request("remove_greater", dlg.buildTicket(), credentials), true);
    }

    private void cmdClear() {
        int res = JOptionPane.showConfirmDialog(this,
                lm.get("dialog.confirm.clear"), lm.get("dialog.confirm"), JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        sendRequest(new Request("clear", "", credentials), true);
    }

    private void cmdRemoveByPrice() {
        String priceStr = JOptionPane.showInputDialog(this, lm.get("dialog.remove_all_by_price"));
        if (priceStr == null || priceStr.isBlank()) return;
        try {
            Long price = Long.valueOf(priceStr.trim());
            sendRequest(new Request("remove_all_by_price", price, credentials), true);
        } catch (NumberFormatException e) {
            showError("Invalid price");
        }
    }

    private void cmdRemoveByType() {
        TicketType[] types = TicketType.values();
        TicketType selected = (TicketType) JOptionPane.showInputDialog(this,
                lm.get("dialog.remove_any_by_type"), lm.get("cmd.remove_any_by_type"),
                JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
        if (selected == null) return;
        sendRequest(new Request("remove_any_by_type", selected, credentials), true);
    }

    private void cmdSimple(String cmd) {
        Response resp = safeCall(new Request(cmd, "", credentials));
        if (resp != null) {
            showInfoDialog(resp.getMessage() + (resp.getData() != null ? "\n" + resp.getData() : ""));
        }
        if (cmd.equals("remove_head") || cmd.equals("clear")) refreshData();
    }

    private void cmdScript() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(lm.get("msg.script.choose"));
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        GuiScriptExecutor executor = new GuiScriptExecutor(network, credentials);
        try {
            executor.executeScript(file.getAbsolutePath());
            showInfoDialog(lm.get("msg.script.done"));
            refreshData();
        } catch (Exception e) {
            showError(lm.get("msg.script.error") + ": " + e.getMessage());
        }
    }

    private void logout() {
        refreshTimer.cancel();
        visualPanel.stopAnimation();
        dispose();
        SwingUtilities.invokeLater(() -> {
            AuthDialog auth = new AuthDialog(null);
            auth.setVisible(true);
            if (auth.getCredentials() != null) {
                MainWindow win = new MainWindow(auth.getCredentials(), auth.getNetwork());
                win.setVisible(true);
            }
        });
    }

    private void refreshData() {
        if (refreshing) return;
        refreshing = true;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<Ticket> tickets = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                try {
                    Response resp = network.sendWithRetry(new Request("show", "", credentials));
                    if (resp != null && resp.isSuccess() && resp.getData() instanceof List<?> list) {
                        for (Object obj : list) {
                            if (obj instanceof Ticket t) tickets.add(t);
                        }
                        Response ownerResp = network.sendWithRetry(new Request("get_owners", "", credentials));
                        if (ownerResp != null && ownerResp.isSuccess() && ownerResp.getData() instanceof Map<?,?> om) {
                            ownerMap.clear();
                            for (Map.Entry<?,?> e : om.entrySet()) {
                                if (e.getKey() instanceof Integer id && e.getValue() instanceof String login) {
                                    ownerMap.put(id, login);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                }
                return null;
            }

            @Override
            protected void done() {
                tableModel.setData(tickets, ownerMap);
                visualPanel.setTickets(tickets, ownerMap);
                refreshing = false;
            }
        };
        worker.execute();
    }

    private void startAutoRefresh() {
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> refreshData());
            }
        }, 3000, 3000);
    }
    private void sendRequest(Request req, boolean thenRefresh) {
        SwingWorker<Response, Void> w = new SwingWorker<>() {
            @Override protected Response doInBackground() { return safeCall(req); }
            @Override protected void done() {
                try {
                    Response r = get();
                    if (r == null) { showError(lm.get("msg.server_error")); return; }
                    if (r.isSuccess()) {
                        if (thenRefresh) refreshData();
                    } else {
                        showError(r.getMessage());
                    }
                } catch (Exception e) { showError(e.getMessage()); }
            }
        };
        w.execute();
    }

    private Response safeCall(Request req) {
        return network.sendWithRetry(req);
    }

    private boolean isOwner(Ticket t) {
        String owner = ownerMap.getOrDefault(t.getId(), "");
        return owner.isEmpty() || owner.equals(credentials.getLogin());
    }

    private void showTicketInfo(Ticket t) {
        LocaleManager lm2 = LocaleManager.getInstance();
        String info = String.format(
                "ID: %d\n%s: %s\nX: %s, Y: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                t.getId(),
                lm2.get("table.name"), t.getName(),
                t.getCoordinates() != null ? t.getCoordinates().getX() : "-",
                t.getCoordinates() != null ? t.getCoordinates().getY() : "-",
                lm2.get("table.creation_date"), lm2.formatDate(t.getCreationDate()),
                lm2.get("table.price"), t.getPrice() != null ? lm2.formatNumber(t.getPrice()) : "-",
                lm2.get("table.type"), t.getType(),
                lm2.get("table.venue_name"), t.getVenue() != null ? t.getVenue().getName() : "-"
        );

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTextArea area = new JTextArea(info);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        if (isOwner(t)) {
            JButton editBtn = new JButton(lm.get("cmd.update"));
            editBtn.addActionListener(e -> {
                Window w = SwingUtilities.getWindowAncestor(panel);
                if (w instanceof JDialog d) d.dispose();
                editTicket(t);
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(editBtn);
            panel.add(bp, BorderLayout.SOUTH);
        }

        JOptionPane.showMessageDialog(this, panel,
                lm.get("dialog.ticket.info"), JOptionPane.PLAIN_MESSAGE);
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, lm.get("dialog.info"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, lm.get("dialog.error"), JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoDialog(String msg) {
        JTextArea area = new JTextArea(msg);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                lm.get("dialog.info"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateAllTexts() {
        setTitle(lm.get("main.title"));
        userLabel.setText(lm.get("main.user") + ": " + credentials.getLogin());
        langLabel.setText(lm.get("main.lang") + ":");

        tabbedPane.setTitleAt(0, lm.get("main.tab.table"));
        tabbedPane.setTitleAt(1, lm.get("main.tab.visual"));
        btnAdd.setText(lm.get("cmd.add"));
        btnUpdate.setText(lm.get("cmd.update"));
        btnRemove.setText(lm.get("cmd.remove"));
        btnRemoveById.setText(lm.get("cmd.remove_by_id"));
        btnRemoveGreater.setText(lm.get("cmd.remove_greater"));
        btnRemoveHead.setText(lm.get("cmd.remove_head"));
        btnHead.setText(lm.get("cmd.head"));
        btnClear.setText(lm.get("cmd.clear"));
        btnRemoveByPrice.setText(lm.get("cmd.remove_all_by_price"));
        btnRemoveByType.setText(lm.get("cmd.remove_any_by_type"));
        btnMinByVenue.setText(lm.get("cmd.min_by_venue"));
        btnShow.setText(lm.get("cmd.show"));
        btnInfo.setText(lm.get("cmd.info"));
        btnHelp.setText(lm.get("cmd.help"));
        btnScript.setText(lm.get("cmd.execute_script"));
        btnLogout.setText(lm.get("cmd.logout"));

        tableModel.refreshColumnNames(tablePanel.getTable());
    }
}
