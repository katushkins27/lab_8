package client.gui;
import common.data.Ticket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class TablePanel extends JPanel{
    private final LocaleManager lm = LocaleManager.getInstance();
    private final TicketTableModel model;
    private final JTable table;
    //private final TableRowSorter<TicketTableModel> rowSorter;

    private JComboBox<String> filterColumnCombo;
    private JTextField filterValueField;
    private JButton filterBtn, clearBtn;
    private JLabel filterLabel, columnLabel, valueLabel;

    private Consumer<Ticket> onEdit;
    private Consumer<Ticket> onDelete;

    public TablePanel(TicketTableModel model){
        this.model = model;
        this.table = new JTable(model);

        setLayout(new BorderLayout(6,6));
        setOpaque(false);
        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        lm.addChangeListener(() -> {
            model.refreshColumnNames(table);
            rebuildFilterCombo();
            updateTexts();
        });

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            boolean asc = true;
            int lastCol = -1;
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col >= 0) {
                    if (col == lastCol) { asc = !asc; } else { asc = true; lastCol = col; }
                    model.setSort(col, asc);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Ticket t = getSelectedTicket();
                    if (t != null && onEdit != null) onEdit.accept(t);
                }
            }
        });


        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");
        editItem.addActionListener(ev -> {
            Ticket t = getSelectedTicket(); if (t != null && onEdit != null) onEdit.accept(t); });
        deleteItem.addActionListener(ev -> {
            Ticket t = getSelectedTicket(); if (t != null && onDelete != null) onDelete.accept(t); });
        popup.add(editItem);
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);
    }

    private JPanel buildFilterBar(){
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(4,4,0,4));

        filterLabel = new JLabel(lm.get("table.filter.label") + ":");
        columnLabel = new JLabel(lm.get("table.filter.column") + ":");
        valueLabel = new JLabel(lm.get("table.filter.value") + ":");

        filterColumnCombo = new JComboBox<>(buildColumnNames());
        filterColumnCombo.setPreferredSize(new Dimension(130,26));

        filterValueField = new JTextField(14);
        filterValueField.setPreferredSize(new Dimension(140,26));

        filterBtn = new JButton(lm.get("table.filter.btn"));
        clearBtn = new JButton(lm.get("table.filter.clear"));

        styleSmallBtn(filterBtn, new Color(66,135,245));
        styleSmallBtn(clearBtn, new Color(200,76,60));

        filterBtn.addActionListener(e -> applyFilter());
        clearBtn.addActionListener(e -> clearFilter());
        filterValueField.addActionListener(e -> applyFilter());

        bar.add(filterLabel);
        bar.add(columnLabel);
        bar.add(filterColumnCombo);
        bar.add(valueLabel);
        bar.add(filterValueField);
        bar.add(filterBtn);
        bar.add(clearBtn);

        return bar;
    }

    private String[] buildColumnNames(){
        String[] names = new String[TicketTableModel.COL_COUNT];
        for (int i =0; i< TicketTableModel.COL_COUNT; i++){
            names[i] = model.getColumnName(i);
        }
        return names;
    }

    private void rebuildFilterCombo(){
        int sel = filterColumnCombo.getSelectedIndex();
        filterColumnCombo.setModel(new DefaultComboBoxModel<>(buildColumnNames()));
        if (sel >= 0 && sel < filterColumnCombo.getItemCount()) filterColumnCombo.setSelectedIndex(sel);
    }

    private void applyFilter(){
        int col = filterColumnCombo.getSelectedIndex();
        String val = filterValueField.getText();
        model.setFilter(col,val);
    }

    private void clearFilter(){
        filterValueField.setText("");
        model.clearFilter();
    }

    private JScrollPane buildTable(){
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setSelectionBackground(new Color(200,220,255));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220,225,235));
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {45,120,80,120,80,90,70,120,80,130,100};
        for (int i = 0; i< widths.length && i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 252));
                }
                setBorder(new EmptyBorder(0, 4, 0, 4));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210,215,230)));
        return scroll;
    }

    private void styleSmallBtn(JButton btn, Color color){
        btn.setBackground(color);
        btn.setForeground(color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN,11));
        btn.setPreferredSize(new Dimension(110,26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void updateTexts() {
        filterLabel.setText(lm.get("table.filter.label") + ":");
        columnLabel.setText(lm.get("table.filter.column") + ":");
        valueLabel.setText(lm.get("table.filter.value") + ":");
        filterBtn.setText(lm.get("table.filter.btn"));
        clearBtn.setText(lm.get("table.filter.clear"));
    }

    public Ticket getSelectedTicket(){
        int viewRow = table.getSelectedRow();
        if (viewRow<0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getTicketAt(modelRow);
    }
    public void setOnEdit(Consumer<Ticket> cb) {this.onEdit = cb;}
    public void setOnDelete(Consumer<Ticket> cb) {this.onDelete = cb;}
    public JTable getTable() {return table;}


}