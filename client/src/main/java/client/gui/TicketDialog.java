package client.gui;
import common.data.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.security.KeyPair;

public class TicketDialog extends JDialog{
    private final LocaleManager lm = LocaleManager.getInstance();
    private final boolean isEdit;

    private JTextField idField;
    private JTextField nameField;
    private JTextField coordXField;
    private JTextField coordYField;
    private JTextField priceField;
    private JComboBox<TicketType> typeCombo;
    private JTextField venueIdField;
    private JTextField venueNameField;
    private JTextField venueCapacityField;
    private JTextField venueStreetField;
    private JTextField venueZipField;
    private JTextField locXField;
    private JTextField locYField;
    private JTextField locZField;
    private JTextField locNameField;

    private JButton okBtn, cancelBtn;
    private boolean confirmed = false;

    public TicketDialog(Window owner, boolean isEdit){
        super(owner, isEdit ? LocaleManager.getInstance().get("ticket.dialog.edit")
                        : LocaleManager.getInstance().get("ticket.dialog.add"),
                ModalityType.APPLICATION_MODAL);
        this.isEdit = isEdit;
        buildUI();
        pack();
        setMinimumSize(new Dimension(480,600));
        setLocationRelativeTo(owner);
    }

    private void buildUI(){
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(new EmptyBorder(16,20,16,20));
        root.setBackground(new Color(248,249,252));

        JScrollPane scroll = new JScrollPane(buildFromPanel());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);
        root.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildFromPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel basicSection = createSection(lm.get("ticket.section.basic"));
        GridBagConstraints gbc = createGbc();
        JPanel basicGrid = new JPanel(new GridBagLayout());
        basicGrid.setOpaque(false);

        if (isEdit) {
            idField = new JTextField(8);
            addRow(basicGrid, gbc, 0, lm.get("ticket.id"), idField);
        }
        nameField = new JTextField(20);
        coordXField = new JTextField(10);
        coordYField = new JTextField(10);
        priceField = new JTextField(10);
        typeCombo = new JComboBox<>(TicketType.values());

        int r = 0;
        if (isEdit) r = 1;
        addRow(basicGrid, gbc, r++, lm.get("ticket.name"), nameField);
        addRow(basicGrid, gbc, r++, lm.get("ticket.coord_x"), coordXField);
        addRow(basicGrid, gbc, r++, lm.get("ticket.coord_y"), coordYField);
        addRow(basicGrid, gbc, r++, lm.get("ticket.price"), priceField);
        addRow(basicGrid, gbc, r, lm.get("ticket.type"), typeCombo);

        basicSection.add(basicGrid);
        panel.add(basicSection);
        panel.add(Box.createVerticalStrut(8));

        JPanel venueSection = createSection(lm.get("ticket.section.venue"));
        JPanel venueGrid = new JPanel(new GridBagLayout());
        venueGrid.setOpaque(false);
        GridBagConstraints gbc2 = createGbc();

        venueIdField = new JTextField(10);
        venueNameField = new JTextField(20);
        venueCapacityField = new JTextField(10);
        venueStreetField = new JTextField(20);
        venueZipField = new JTextField(10);

        addRow(venueGrid, gbc2, 0, lm.get("ticket.venue_id"), venueIdField);
        addRow(venueGrid, gbc2, 1, lm.get("ticket.venue_name"), venueNameField);
        addRow(venueGrid, gbc2, 2, lm.get("ticket.venue_capacity"), venueCapacityField);
        addRow(venueGrid, gbc2, 3, lm.get("ticket.venue_street"), venueStreetField);
        addRow(venueGrid, gbc2, 4, lm.get("ticket.venue_zipcode"), venueZipField);

        venueSection.add(venueGrid);
        panel.add(venueSection);
        panel.add(Box.createVerticalStrut(8));

        JPanel locSection = createSection(lm.get("ticket.section.location"));
        JPanel locGrid = new JPanel(new GridBagLayout());
        locGrid.setOpaque(false);
        GridBagConstraints gbc3 = createGbc();

        locXField = new JTextField(10);
        locYField = new JTextField(10);
        locZField = new JTextField(10);
        locNameField = new JTextField(20);

        addRow(locGrid, gbc3, 0, lm.get("ticket.location_x"), locXField);
        addRow(locGrid, gbc3, 1, lm.get("ticket.location_y"), locYField);
        addRow(locGrid, gbc3, 2, lm.get("ticket.location_z"), locZField);
        addRow(locGrid, gbc3, 3, lm.get("ticket.location_name"), locNameField);

        locSection.add(locGrid);
        panel.add(locSection);

        return panel;
    }

    private JPanel createSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1, true),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), new Color(80, 100, 160));
        section.setBorder(border);
        return section;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText + ":");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private JPanel buildButtonPanel(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,4));
        p.setOpaque(false);
        okBtn = new JButton(lm.get("ticket.btn.ok"));
        cancelBtn = new JButton(lm.get("ticket.btn.cancel"));

        styleBtn(okBtn, new Color(66,135,245));
        styleBtn(cancelBtn, new Color(150, 155, 165));

        okBtn.addActionListener(e -> {
            if (validateAndConfirm()) dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        p.add(cancelBtn);
        p.add(okBtn);
        return p;
    }

    private void styleBtn(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private boolean validateAndConfirm(){
        try{
            buildTicket();
            confirmed = true;
            return  true;
        } catch (Exception e){
            JOptionPane.showMessageDialog(this,lm.get("ticket.error.invalid") + ":\n" + e.getMessage(),
                    lm.get("dialog.error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isConfirmed(){
        return confirmed;
    }

    public void fillFrom(Ticket t){
        if (isEdit && idField != null) idField.setText(String.valueOf(t.getId()));
        nameField.setText(t.getName() != null ? t.getName(): "");
        if (t.getCoordinates() != null) {
            coordXField.setText(String.valueOf(t.getCoordinates().getX()));
            coordYField.setText(String.valueOf(t.getCoordinates().getY()));
        }
        priceField.setText(t.getPrice() != null ? String.valueOf(t.getPrice()) : "");
        if (t.getType() != null) typeCombo.setSelectedItem(t.getType());
        if (t.getVenue() != null) {
            Venue v = t.getVenue();
            venueIdField.setText(String.valueOf(v.getID()));
            venueNameField.setText(v.getName() != null ? v.getName() : "");
            venueCapacityField.setText(String.valueOf(v.getCapacity()));
            if (v.getAddress() != null) {
                venueStreetField.setText(v.getAddress().getStreet() != null ? v.getAddress().getStreet() : "");
                venueZipField.setText(v.getAddress().getZipCode() != null ? v.getAddress().getZipCode() : "");
                if (v.getAddress().getTown() != null) {
                    Location loc = v.getAddress().getTown();
                    locXField.setText(String.valueOf(loc.getX()));
                    locYField.setText(String.valueOf(loc.getY()));
                    locZField.setText(String.valueOf(loc.getZ()));
                    locNameField.setText(loc.getName() != null ? loc.getName() : "");
                }
            }
        }
    }

    public Ticket buildTicket() {
        Ticket t = new Ticket();

        if (isEdit && idField != null && !idField.getText().isBlank()) {
            t.setID(Integer.parseInt(idField.getText().trim()));
        }

        t.setName(nameField.getText().trim());
        if (t.getName().isEmpty()) throw new IllegalArgumentException("Название не может быть пустым");

        int cx = Integer.parseInt(coordXField.getText().trim());
        long cy = Long.parseLong(coordYField.getText().trim());
        t.setCoordinates(new Coordinates(cx, cy));

        String priceStr = priceField.getText().trim();
        if (!priceStr.isEmpty()) {
            long price = Long.parseLong(priceStr);
            t.setPrice(price);
        }
        t.setType((TicketType) typeCombo.getSelectedItem());
        t.setCreationDate(java.time.LocalDateTime.now());

        long venueId = Long.parseLong(venueIdField.getText().trim());
        String venueName = venueNameField.getText().trim();
        if (venueName.isEmpty()) throw new IllegalArgumentException("Название площадки не может быть пустым");
        int venueCap = Integer.parseInt(venueCapacityField.getText().trim());
        if (venueCap <= 0) throw new IllegalArgumentException("Вместимость должна быть > 0");

        String street = venueStreetField.getText().trim();
        if (street.isEmpty()) throw new IllegalArgumentException("Улица не может быть пустой");
        String zip = venueZipField.getText().trim();

        double lx = Double.parseDouble(locXField.getText().trim());
        long ly = Long.parseLong(locYField.getText().trim());
        float lz = Float.parseFloat(locZField.getText().trim());
        String lname = locNameField.getText().trim();
        if (lname.isEmpty()) lname = null;

        Location location = new Location(lname, lx, ly, lz);
        Address address = new Address(street, zip.isEmpty() ? null : zip, location);
        Venue venue = new Venue(venueId, venueName, venueCap, address);
        t.setVenue(venue);

        return t;
    }

    public String setIdArg(){
        if (isEdit && idField != null) return idField.getText().trim();
        return "";
    }
}
