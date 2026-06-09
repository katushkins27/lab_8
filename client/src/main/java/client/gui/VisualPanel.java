package client.gui;
import common.data.Ticket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.Timer;

public class VisualPanel extends JPanel {
    private static final int TICKET_W = 80;
    private static final int TICKET_H = 50;
    private static final int ANIM_DURATION_MS = 600;
    private static final Color[] USER_COLORS={
            new Color(66, 135, 245),
            new Color(230, 80, 80),
            new Color(80, 185, 80),
            new Color(220, 160, 30),
            new Color(160, 80, 200),
            new Color(30, 190, 200),
            new Color(240, 100, 160),
            new Color(120, 180, 60)
    };

    private static class TicketShape{
        Ticket ticket;
        String owner;
        double screenX, screenY;
        double targetX, targetY;
        float alpha;
        float scale;
        boolean appearing;
        long animStart;
        boolean dead;
        Color color;

        TicketShape(Ticket t, String owner, double x, double y, Color color){
            this.ticket = t;
            this.owner = owner;
            this.targetX = x;
            this.screenX = x;
            this.targetY = y;
            this.screenY = y;
            this.alpha = 0f;
            this.scale = 0f;
            this.appearing = true;
            this.animStart = System.currentTimeMillis();
            this.color = color;
            this.dead = false;
        }
    }

    private final List<TicketShape> shapes = new ArrayList<>();
    private  final Map<String, Color> userColors = new LinkedHashMap<>();
    private  int colorIndex = 0;
    private TicketShape hoveredShape = null;
    private TicketShape selectedShape = null;
    private BiConsumer<Ticket, Point> onTicketClick;
    private java.util.function.Consumer<Ticket> onTicketDoubleClick;
    private final Timer animTimer;
    private Point dragStart;
    private int panX =0, panY = 0;

    public VisualPanel(){
        setBackground(new Color(240,243,250));
        setPreferredSize(new Dimension(800,500));
        animTimer = new Timer(16, e -> {
            updateAnimations();
            repaint();
        });
        animTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TicketShape hit = findShape(e.getX(), e.getY());
                if (hit != null){
                    selectedShape = hit;
                    if (e.getClickCount() == 2 && onTicketDoubleClick != null){
                        onTicketDoubleClick.accept(hit.ticket);
                    } else if (onTicketClick != null){
                        onTicketClick.accept(hit.ticket, e.getLocationOnScreen());
                    }
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e){
                dragStart = e.getPoint();
            }
            @Override
            public void mouseReleased(MouseEvent e){
                dragStart = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e){
                TicketShape prev = hoveredShape;
                hoveredShape = findShape(e.getX(), e.getY());
                if (prev != hoveredShape) repaint();
                setCursor(hoveredShape != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null){
                    panX += e.getX() - dragStart.x;
                    panY += e.getY() - dragStart.y;
                    dragStart = e.getPoint();
                    repaint();
                }
            }
        });
    }

    public void setOnTicketClick(BiConsumer<Ticket,Point> cb){
        this.onTicketClick = cb;
    }
    public void setOnTicketDoubleClick(java.util.function.Consumer<Ticket> cb) {
        this.onTicketDoubleClick = cb;
    }

    public void setTickets(List<Ticket> tickets, Map<Integer,String> ownerMap){
        Set<Integer> newIds = new HashSet<>();
        for (Ticket t : tickets) newIds.add(t.getId());
        for (TicketShape s : shapes){
            if (!newIds.contains(s.ticket.getId()) && !s.dead){
                s.appearing = false;
                s.animStart = System.currentTimeMillis();
                s.dead = true;
            }
        }
        for (Ticket t : tickets){
            String owner = ownerMap != null ? ownerMap.getOrDefault(t.getId(),""): "";
            Color color = getUserColor(owner);
            Optional<TicketShape> existing = shapes.stream()
                    .filter(s -> s.ticket.getId() == t.getId() && !s.dead).findFirst();
            if (existing.isPresent()) {
                TicketShape s = existing.get();
                s.ticket = t;
                s.owner = owner;
                s.color = color;
                double[] pos = calcPosition(t, getWidth()>0 ? getWidth(): 800, getHeight()>0 ? getHeight(): 500);
                s.targetX = pos[0];
                s.targetY = pos[1];
            } else{
                double[] pos = calcPosition(t, getWidth() > 0 ? getWidth() : 800, getHeight() > 0 ? getHeight() : 500);
                shapes.add(new TicketShape(t, owner, pos[0], pos[1], color));
            }
        }
    }

    private double[] calcPosition(Ticket t, int w, int h){
        int margin = 60;
        int cx = t.getCoordinates() != null ? t.getCoordinates().getX():0;
        long cy = t.getCoordinates() != null ? t.getCoordinates().getY() : 0;
        double nx = ((cx % 200) + 200) % 200;
        double ny = ((cy % 200) + 200) % 200;
        double x = margin + (nx / 200.0) * (w - 2 * margin - TICKET_W);
        double y = margin + (ny / 200.0) * (h - 2 * margin - TICKET_H);
        return new double[]{x, y};
    }

    private Color getUserColor(String owner){
        if (!userColors.containsKey(owner)){
            userColors.put(owner, USER_COLORS[colorIndex % USER_COLORS.length]);
            colorIndex++;
        }
        return userColors.get(owner);
    }
    private void updateAnimations(){
        long now = System.currentTimeMillis();
        Iterator<TicketShape> iter = shapes.iterator();
        while (iter.hasNext()){
            TicketShape s = iter.next();
            long elapsed = now - s.animStart;
            float progress = Math.min(1f, (float) elapsed/ANIM_DURATION_MS);
            float eased = easeOutCubic(progress);
            if(s.appearing){
                s.alpha = eased;
                s.scale = eased;
            }else{
                s.alpha= 1f - eased;
                s.scale = 1f - eased * 0.3f;
                if (progress >= 1f){
                    iter.remove();
                    continue;
                }
            }
            s.screenX += (s.targetX - s.screenX) * 0.12;
            s.screenY += (s.targetY - s.screenY) * 0.12;
        }
    }

    private float easeOutCubic(float t){
        return 1 - (float) Math.pow(1 - t, 3);
    }
    private TicketShape findShape(int mx, int my){
        int tx = mx - panX, ty = my - panY;
        for (int i = shapes.size() - 1; i >= 0; i--) {
            TicketShape s = shapes.get(i);
            if (s.alpha < 0.1f) continue;
            double x = s.screenX, y = s.screenY;
            if (tx >= x && tx <= x + TICKET_W && ty >= y && ty <= y + TICKET_H) return s;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawGrid(g2);
        g2.translate(panX,panY);
        for (TicketShape s: shapes){
            drawTicketShape(g2,s);
        }
        g2.translate(-panX, -panY);
        g2.setFont(new Font("SansSerif", Font.ITALIC,11));
        g2.setColor(new Color(160, 165, 180));
        String hint = LocaleManager.getInstance().get("visual.click_hint");
        g2.drawString(hint, 10, getHeight() - 10);
        drawLegend(g2);
        g2.dispose();
    }

    private void drawGrid(Graphics2D g2){
        g2.setColor(new Color(225, 228, 238));
        g2.setStroke(new BasicStroke(0.5f));
        int step = 50;
        for (int x =0 ; x<getWidth(); x+=step)
            g2.drawLine(x,0,x,getHeight());
        for (int y = 0; y < getHeight(); y += step)
            g2.drawLine(0, y, getWidth(), y);
    }

    private void drawTicketShape(Graphics2D g2, TicketShape s){
        if (s.alpha <= 0f) return;
        float alpha = Math.max(0f, Math.min(1f, s.alpha));
        Composite origComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        double cx = s.screenX + TICKET_W/2.0;
        double cy = s.screenY + TICKET_H / 2.0;
        double hw = (TICKET_W / 2.0) * s.scale;
        double hh = (TICKET_H / 2.0) * s.scale;

        double x = cx - hw, y = cy - hh;
        double w = hw * 2, h = hh * 2;

        boolean isSelected = (s == selectedShape);
        boolean isHovered = (s == hoveredShape);

        g2.setColor(new Color(0, 0, 0, (int) (30 * alpha)));
        g2.fill(new RoundRectangle2D.Double(x + 3, y + 3, w, h, 14, 14));

        Color base = s.color;
        if (isSelected) base = base.brighter();
        else if (isHovered) base = base.brighter().brighter();

        g2.setColor(base);
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 14, 14));

        g2.setColor(base.darker());
        g2.fill(new RoundRectangle2D.Double(x, y, 8, h, 8, 8));

        g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.2f));
        g2.setColor(isSelected ? Color.WHITE : base.darker().darker());
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 14, 14));

        drawPerforation(g2, x, y, w, h, alpha);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, (int) (11*s.scale)));
        String nameText = s.ticket.getName();
        if (nameText.length()>9) nameText = nameText.substring(0,8)+"…";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(nameText, (float) (x+12), (float) (cy-3));
        g2.setFont(new Font("SansSerif", Font.PLAIN, (int) (9 * s.scale)));
        String idText = "#" + s.ticket.getId();
        g2.drawString(idText, (float) (x + 12), (float) (cy + 10));

        if (s.ticket.getPrice() != null){
            g2.setFont(new Font("SansSerif", Font.BOLD, (int) (8*s.scale)));
            g2.setColor(new Color(255, 255, 200));
            String priceText = s.ticket.getPrice().toString();
            int pw = fm.stringWidth(priceText);
            g2.drawString(priceText, (float) (x + w - pw - 4), (float) (y + h - 5));
        }
        g2.setComposite(origComposite);
    }

    private void drawPerforation(Graphics2D g2, double x, double y, double w, double h, float alpha){
        g2.setColor(new Color(255, 255, 255, (int)(80 * alpha)));
        int r = 4;
        g2.fillOval((int)(x - r/2.0), (int)(y + h/2.0 - r/2.0), r, r);
        g2.fillOval((int)(x + w - r/2.0), (int)(y + h/2.0 - r/2.0), r, r);

        g2.setColor(new Color(255, 255, 255, (int)(40 * alpha)));
        float[] dash = {3f, 3f};
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash, 0f));
        g2.drawLine((int)(x + 8), (int)(y + h/2.0), (int)(x + w - 8), (int)(y + h/2.0));
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawLegend(Graphics2D g2){
        if (userColors.isEmpty()) return;
        int px =10, py = 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Map.Entry<String, Color> e : userColors.entrySet()){
            g2.setColor(e.getValue());
            g2.fillRoundRect(px, py, 12, 12, 4, 4);
            g2.setColor(new Color(50, 55, 70));
            g2.drawString(e.getKey(), px + 16, py + 11);
            py += 18;
        }
    }
    public void stopAnimation() {
        animTimer.stop();
    }

}
