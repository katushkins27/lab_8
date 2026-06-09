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
            idField.setEditable(false);
            idField.setBackground(new Color(198, 198, 198));
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

        //venueIdField = new JTextField(10);
        venueNameField = new JTextField(20);
        venueCapacityField = new JTextField(10);
        venueStreetField = new JTextField(20);
        venueZipField = new JTextField(10);

        int vRow = 0;
        if (isEdit) {
            venueIdField = new JTextField(10);
            venueIdField.setEditable(false);
            venueIdField.setBackground(new Color(198, 198, 198));
            addRow(venueGrid, gbc2, vRow++, lm.get("ticket.venue_id"), venueIdField);
        }

        addRow(venueGrid, gbc2, vRow++, lm.get("ticket.venue_name"), venueNameField);
        addRow(venueGrid, gbc2, vRow++, lm.get("ticket.venue_capacity"), venueCapacityField);
        addRow(venueGrid, gbc2, vRow++, lm.get("ticket.venue_street"), venueStreetField);
        addRow(venueGrid, gbc2, vRow, lm.get("ticket.venue_zipcode"), venueZipField);

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

        styleBtn(okBtn, new Color(100,178,100));
        styleBtn(cancelBtn, new Color(200,76,60));

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
            if (isEdit && venueIdField != null) {
                venueIdField.setText(String.valueOf(t.getVenue().getID()));
            }
            Venue v = t.getVenue();
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

        try {
            String cxStr = coordXField.getText().trim();
            if (cxStr.isEmpty()) throw new IllegalArgumentException("Координата X билета не может быть пустой");
            String cyStr = coordYField.getText().trim();
            if (cyStr.isEmpty()) throw new IllegalArgumentException("Координата Y билета не может быть пустой");

            int cx = Integer.parseInt(cxStr);
            Long cy = Long.parseLong(cyStr);
            t.setCoordinates(new Coordinates(cx, cy));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Координаты введены некорректно: X (целое), Y (целое)");
        }

        String priceStr = priceField.getText().trim();
        if (!priceStr.isEmpty()) {
            long price = Long.parseLong(priceStr);
            if (price <= 0) throw new IllegalArgumentException("Цена билета должна быть больше 0");
            t.setPrice(price);
        }
        t.setType((TicketType) typeCombo.getSelectedItem());
        t.setCreationDate(java.time.LocalDateTime.now());

        //long venueId = Long.parseLong(venueIdField.getText().trim());
        String venueName = venueNameField.getText().trim();
        if (!venueName.isEmpty()) {
            long venueId = 0L;
            if (isEdit && venueIdField != null && !venueIdField.getText().isBlank()) {
                try {
                    venueId = Long.parseLong(venueIdField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Некорректный ID площадки");
                }
            }

            int venueCap;
            try {
                venueCap = Integer.parseInt(venueCapacityField.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Вместимость площадки должна быть целым числом");
            }
            if (venueCap <= 0) throw new IllegalArgumentException("Вместимость площадки должна быть больше 0");

            String street = venueStreetField.getText().trim();
            if (street.isEmpty()) throw new IllegalArgumentException("Улица не может быть пустой");
            if (street.length() > 61) throw new IllegalArgumentException("Длина улицы не может превышать 61 символ");

            String zip = venueZipField.getText().trim();
            if (zip.isEmpty()) zip = null;
            String lxStr = locXField.getText().trim();
            String lyStr = locYField.getText().trim();
            String lzStr = locZField.getText().trim();
            String lname = locNameField.getText().trim();

            Location location = null;

            if (!lxStr.isEmpty() || !lyStr.isEmpty() || !lzStr.isEmpty() || !lname.isEmpty()) {
                if (lxStr.isEmpty()) throw new IllegalArgumentException("Для локации координата X не может быть пустой");
                if (lyStr.isEmpty()) throw new IllegalArgumentException("Для локации координата Y не может быть пустой");
                if (lzStr.isEmpty()) throw new IllegalArgumentException("Для локации координата Z не может быть пустой");
                try {
                    double lx = lxStr.isEmpty() ? 0.0 : Double.parseDouble(lxStr);
                    Long ly = Long.parseLong(lyStr);
                    Float lz = Float.parseFloat(lzStr);

                    if (lname.length() > 777) throw new IllegalArgumentException("Название локации слишком длинное (<= 777)");
                    String finalLocName = lname.isEmpty() ? null : lname;
                    location = new Location(finalLocName, lx, ly, lz);

                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Координаты локации неверны: X (с точкой), Y (целое), Z (с точкой)");
                }
            }
            Address address = new Address(street, zip, location);
            Venue venue = new Venue(venueId, venueName, venueCap, address);
            t.setVenue(venue);
        } else {
            t.setVenue(null);
        }
        return t;
    }

    public String setIdArg(){
        if (isEdit && idField != null) return idField.getText().trim();
        return "";
    }
}