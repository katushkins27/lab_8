package client.gui;
import common.data.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
public class TicketTableModel extends AbstractTableModel {
    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_COORD_X = 2;
    public static final int COL_COORD_Y = 3;
    public static final int COL_DATE = 4;
    public static final int COL_PRICE = 5;
    public static final int COL_TYPE = 6;
    public static final int COL_VENUE_ID = 7;
    public static final int COL_VENUE_NAME = 8;
    public static final int COL_VENUE_CAP = 9;
    public static final int COL_VENUE_STREET = 10;
    public static final int COL_OWNER = 11;
    public static final int COL_COUNT = 12;

    private final LocaleManager lm = LocaleManager.getInstance();

    private List<Ticket> allTickets = new ArrayList<>();
    private List<Ticket> displayTickets = new ArrayList<>();
    private Map<Integer, String> ownerMap = new HashMap<>();
    private int filterColumnIndex = -1;
    private String filterValue = "";
    private int sortColumn = -1;
    private boolean sortAscending = true;

    private static final String[] COLUMN_KEYS ={
            "table.id", "table.name", "table.coord_x", "table.coord_y",
            "table.creation_date", "table.price", "table.type",
            "table.venue_id", "table.venue_name", "table.venue_capacity",
            "table.venue_street", "table.owner"
    };

    public void setData(List<Ticket> tickets, Map<Integer,String> owners){
        this.allTickets  = new ArrayList<>(tickets);
        this.ownerMap = owners != null ? new HashMap<>(owners): new HashMap<>();
        applyFilterAndSort();
    }

    public void applyFilterAndSort(){
        displayTickets = allTickets.stream().filter(t ->{
            if (filterColumnIndex<0 || filterValue == null || filterValue.isBlank()) return true;
            String cellStr = getCellString(t, filterColumnIndex).toLowerCase();
            return cellStr.contains(filterValue.toLowerCase());
        }).sorted((a,b) ->{
            if (sortColumn < 0) return 0;
            int cmp = compareByColumn(a,b,sortColumn);
            return sortAscending ? cmp: -cmp;
        }).collect(Collectors.toList());
        fireTableDataChanged();
    }

    private String getCellString(Ticket t, int col){
        Object val = getCellValue(t, col);
        return val == null ? "": val.toString();
    }

    private Object getCellValue(Ticket t, int col) {
        return switch (col) {
            case COL_ID -> t.getId();
            case COL_NAME -> t.getName();
            case COL_COORD_X -> t.getCoordinates() != null ? t.getCoordinates().getX() : null;
            case COL_COORD_Y -> t.getCoordinates() != null ? t.getCoordinates().getY() : null;
            case COL_DATE -> t.getCreationDate() != null ? lm.formatDateShort(t.getCreationDate()) : null;
            case COL_PRICE -> t.getPrice() != null ? lm.formatNumber(t.getPrice()) : null;
            case COL_TYPE -> t.getType() != null ? t.getType().name() : null;
            case COL_VENUE_ID -> t.getVenue() != null ? t.getVenue().getID() : null;
            case COL_VENUE_NAME -> t.getVenue() != null ? t.getVenue().getName() : null;
            case COL_VENUE_CAP -> t.getVenue() != null ? lm.formatNumber(t.getVenue().getCapacity()) : null;
            case COL_VENUE_STREET -> t.getVenue() != null && t.getVenue().getAddress() != null ? t.getVenue().getAddress().getStreet() : null;
            case COL_OWNER -> ownerMap.getOrDefault(t.getId(), "");
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareByColumn(Ticket a, Ticket b, int col){
        Object va = getCellValue(a, col);
        Object vb = getCellValue(b, col);
        if (va == null && vb == null) return 0;
        if (va == null) return -1;
        if (vb == null) return 1;
        if (col == COL_ID)     return Long.compare(a.getId(), b.getId());
        if (col == COL_COORD_X) return Integer.compare(a.getCoordinates().getX(), b.getCoordinates().getX());
        if (col == COL_COORD_Y) return Long.compare(a.getCoordinates().getY(), b.getCoordinates().getY());
        if (col == COL_PRICE && a.getPrice() != null && b.getPrice() != null)
            return Long.compare(a.getPrice(), b.getPrice());
        if (col == COL_VENUE_CAP) return Long.compare(a.getVenue().getCapacity(), b.getVenue().getCapacity());
        if (va instanceof Comparable && vb instanceof Comparable) {
            return ((Comparable) va).compareTo(vb);
        }
        return va.toString().compareTo(vb.toString());
    }

    public void setFilter(int columnIndex, String value){
        this.filterColumnIndex =columnIndex;
        this.filterValue = value;
        applyFilterAndSort();
    }

    public void clearFilter(){
        this.filterColumnIndex = -1;
        this.filterValue = "";
        applyFilterAndSort();
    }

    public void setSort(int columnIndex, boolean ascending){
        this.sortColumn = columnIndex;
        this.sortAscending = ascending;
        applyFilterAndSort();
    }

    public Ticket getTicketAt(int row){
        if (row < 0 || row >= displayTickets.size()) return null;
        return displayTickets.get(row);
    }

    public List<Ticket> getAllTickets(){
        return Collections.unmodifiableList(allTickets);
    }
    @Override public int getRowCount() { return displayTickets.size(); }
    @Override public int getColumnCount() { return COL_COUNT; }

    @Override
    public String getColumnName(int col){
        if (col <0 || col >= COLUMN_KEYS.length) return "";
        return lm.get(COLUMN_KEYS[col]);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= displayTickets.size()) return null;
        return getCellValue(displayTickets.get(row), col);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }
    public void refreshColumnNames(JTable table) {
        for (int i=0; i<getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderValue(getColumnName(i));
        }
        table.getTableHeader().repaint();
    }
}
