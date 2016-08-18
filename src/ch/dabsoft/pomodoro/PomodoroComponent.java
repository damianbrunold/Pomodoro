package ch.dabsoft.pomodoro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

public class PomodoroComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    private Preferences prefs = Preferences.userNodeForPackage(Pomodoro.class);
    private Pomodoro frame;
    
    private boolean pressed = false;
    private int x, y;
    
    private BasicStroke stroke1;
    private BasicStroke stroke3;
    private BasicStroke stroke5;

    private Color background = new Color(137, 194, 228);
    private Color borders = Color.DARK_GRAY;
    private Color remaining = Color.RED;
    private Color text = Color.WHITE;
    
    private int size;
    private int radius;
    private int cp;
    private int top;
    private int left;

    private int cx;
    private int cy;

    public int duration = 25;

    private Font font;
    
    public PomodoroComponent(Pomodoro frame) {
        setOpaque(false);
        this.frame = frame;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                PomodoroComponent.this.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                PomodoroComponent.this.mouseReleased(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                PomodoroComponent.this.mouseDragged(e);
            }
        });
        recalculate();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculate();
            }
        });
    }
    
    public void recalculate() {
        size = getWidth() - 3;
        
        radius = size / 2;
        
        cp = size / 100;
        
        top = getY() + 1;
        left = getX() + 1;

        cx = left + radius;
        cy = top + radius;

        int s = Math.max(1, cp / 2);
        stroke1 = new BasicStroke(s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        stroke3 = new BasicStroke(3 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        stroke5 = new BasicStroke(5 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        
        font = new Font(Font.SANS_SERIF, Font.PLAIN, cp * 10);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBackgroundCircle(g2);
        drawRemainingFill(g2);
        drawRemainingText(g2);
    }

    private void drawBackgroundCircle(Graphics2D g) {
        g.setColor(background);
        g.fillOval(left, top, size, size);
        g.setColor(borders);
        g.setStroke(stroke3);
        g.drawOval(left, top, size, size);
    }

    private void drawRemainingFill(Graphics2D g) {
        g.setColor(remaining);
        // TODO
    }

    private void drawRemainingText(Graphics2D g) {
        g.setColor(text);
        g.setFont(font);
        g.setStroke(stroke1);
        String s = Integer.toString(duration); // TODO
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, cx - fm.stringWidth(s) / 2, cy + fm.getDescent() + 3 * cp); // TODO
    }

    private void mouseDragged(MouseEvent e) {
        if (pressed) {
            int newx = e.getXOnScreen();
            int newy = e.getYOnScreen();
            int fx = frame.getX();
            int fy = frame.getY();
            frame.setLocation(fx + newx - x, fy + newy - y);
            x = newx;
            y = newy;
        }
    }

    private void mousePressed(MouseEvent e) {
        pressed = true;
        x = e.getXOnScreen();
        y = e.getYOnScreen();
        if (e.isPopupTrigger()) {
            showSettings(e);
        }
    }

    private void mouseReleased(MouseEvent e) {
        pressed = false;
        prefs.putInt("x", frame.getX());
        prefs.putInt("y", frame.getY());
        if (e.isPopupTrigger()) {
            showSettings(e);
        }
    }

    private void showSettings(MouseEvent e) {
        // TODO show settings menu
        System.out.println("settings");
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 27) {
            frame.exitApp();
        }
        int size;
        switch (e.getKeyCode()) {
        case 107:
        case 49:
            size = frame.getWidth();
            size += 20;
            if (size > 2048) size = 2048;
            frame.setSize(size, size);
            prefs.putInt("size", frame.getWidth());
            break;
            
        case 109:
        case 45:
            size = frame.getWidth();
            size -= 20;
            if (size < 100) size = 100;
            frame.setSize(size, size);
            prefs.putInt("size", frame.getWidth());
            break;
            
        case 96:
        case 48:
            frame.setSize(400, 400);
            frame.setLocationRelativeTo(null);
            prefs.putInt("size", 400);
            prefs.remove("x");
            prefs.remove("y");
            break;

        // TODO handle duration increase/decrease
            
        default:
            System.out.println(e.getKeyCode());
            break;
        }
    }
    
}
