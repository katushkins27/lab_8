package client.gui;

import common.auth.Credentials;
import common.network.Request;
import common.network.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class AuthDialog extends JDialog{
    private final LocaleManager lm = LocaleManager.getInstance();

    private JTextField hostField;
    private JTextField portField;
    private JTextField loginField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JLabel hostLabel, portLabel, loginLabel, passwordLabel;
    private JButton loginBtn, registerBtn;

    private Credentials resultCredentials;
    private client.NetworkManager network;

    public AuthDialog(Frame owner) {
        super(owner, true);
        buildUI();
        lm.addChangeListener(this::updateTexts);
    }

    private void buildUI() {
        setTitle(lm.get("auth.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        root.setBackground(new Color(245, 246, 250));
        titleLabel = new JLabel(lm.get("auth.title"), SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(titleLabel, BorderLayout.NORTH);


        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        hostLabel = addFormRow(form, gbc, row++, lm.get("auth.host"));
        hostField = new JTextField("localhost", 18);
        gbc.gridx = 1; gbc.gridy = row - 1; form.add(hostField, gbc);

        portLabel = addFormRow(form, gbc, row++, lm.get("auth.port"));
        portField = new JTextField("8080", 18);
        gbc.gridx = 1; gbc.gridy = row - 1; form.add(portField, gbc);

        loginLabel = addFormRow(form, gbc, row++, lm.get("auth.login"));
        loginField = new JTextField(18);
        gbc.gridx = 1; gbc.gridy = row - 1; form.add(loginField, gbc);

        passwordLabel = addFormRow(form, gbc, row++, lm.get("auth.password"));
        passwordField = new JPasswordField(18);
        gbc.gridx = 1; gbc.gridy = row - 1; form.add(passwordField, gbc);

        root.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.setOpaque(false);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setOpaque(false);
        loginBtn = new JButton(lm.get("auth.btn.login"));
        registerBtn = new JButton(lm.get("auth.btn.register"));
        styleButton(loginBtn, new Color(66, 135, 245));
        styleButton(registerBtn, new Color(80, 185, 110));
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(new Color(200, 60, 60));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        south.add(btnPanel, BorderLayout.CENTER);
        south.add(statusLabel, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        loginBtn.addActionListener(this::onLogin);
        registerBtn.addActionListener(this::onRegister);
        passwordField.addActionListener(this::onLogin);

        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(360, 300));
        setLocationRelativeTo(getOwner());
    }

    private JLabel addFormRow(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel label = new JLabel(text + ":");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(label, gbc);
        gbc.weightx = 0.7;
        return label;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 34));
    }

    private void onLogin(ActionEvent e) {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            setStatus(lm.get("auth.error.empty"), true);
            return;
        }
        Credentials creds = new Credentials(login, password);
        if (!connectNetwork()) return;
        try {
            Request req = new Request("auth", "", creds);
            Response resp = network.sendWithRetry(req);
            if (resp == null) { setStatus(lm.get("msg.server_error"), true); return; }
            if (resp.isSuccess()) {
                resultCredentials = creds;
                dispose();
            } else {
                setStatus(resp.getMessage(), true);
            }
        } catch (Exception ex) {
            setStatus(lm.get("auth.error.connect") + ": " + ex.getMessage(), true);
        }
    }

    private void onRegister(ActionEvent e) {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            setStatus(lm.get("auth.error.empty"), true);
            return;
        }
        Credentials creds = new Credentials(login, password);
        if (!connectNetwork()) return;
        try {
            Request req = new Request("register", "", creds);
            Response resp = network.sendWithRetry(req);
            if (resp == null) { setStatus(lm.get("msg.server_error"), true); return; }
            if (resp.isSuccess()) {
                setStatus(lm.get("auth.success.register"), false);
            } else {
                setStatus(resp.getMessage(), true);
            }
        } catch (Exception ex) {
            setStatus(lm.get("auth.error.connect") + ": " + ex.getMessage(), true);
        }
    }
    private boolean connectNetwork() {
        String host = hostField.getText().trim();
        int port;
        try { port = Integer.parseInt(portField.getText().trim()); }
        catch (NumberFormatException ex) { setStatus("Invalid port", true); return false; }
        try {
            if (network != null) { try { network.close(); } catch (IOException ignored) {} }
            network = new client.NetworkManager(host, port);
            return true;
        } catch (IOException ex) {
            setStatus(lm.get("auth.error.connect") + ": " + ex.getMessage(), true);
            return false;
        }
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setForeground(error ? new Color(200, 60, 60) : new Color(40, 160, 80));
        statusLabel.setText(msg);
    }

    private void updateTexts() {
        setTitle(lm.get("auth.title"));
        titleLabel.setText(lm.get("auth.title"));
        hostLabel.setText(lm.get("auth.host") + ":");
        portLabel.setText(lm.get("auth.port") + ":");
        loginLabel.setText(lm.get("auth.login") + ":");
        passwordLabel.setText(lm.get("auth.password") + ":");
        loginBtn.setText(lm.get("auth.btn.login"));
        registerBtn.setText(lm.get("auth.btn.register"));
    }
    public Credentials getCredentials() { return resultCredentials; }
    public client.NetworkManager getNetwork() { return network; }
    public String getHost() { return hostField.getText().trim(); }
    public int getPort() {
        try { return Integer.parseInt(portField.getText().trim()); }
        catch (NumberFormatException e) { return 8080; }
    }
}
