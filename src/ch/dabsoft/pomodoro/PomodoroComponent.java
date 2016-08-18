package ch.dabsoft.pomodoro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

public class PomodoroComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    private Preferences prefs = Preferences.userNodeForPackage(Pomodoro.class);
    private Pomodoro frame;
    
    private boolean pressed = false;
    private int x, y;
    
    private BasicStroke stroke1;
    private BasicStroke stroke3;
    private BasicStroke stroke5;

    private Color background = new Color(128, 0, 0);
    private Color borders = Color.DARK_GRAY;
    private Color remaining = Color.RED;
    private Color text = Color.WHITE;
    
    private int width;
    private int height;
    
    private int cp;
    private int top;
    private int left;

    private int cx;
    private int cy;

    public boolean started = false;
    public long startTime = -1L;
    public int duration = 25 * 60;
    public int remainingDuration = duration;

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
        width = getWidth() - 3;
        height = getHeight() - 3;
        
        cp = height / 100;
        
        top = getY() + 1;
        left = getX() + 1;

        cx = left + width / 2;
        cy = top + height / 2;

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
        
        drawBackgroundRect(g2);
        drawRemainingFill(g2);
        drawRemainingText(g2);
    }

    private void drawBackgroundRect(Graphics2D g) {
        g.setColor(background);
        g.fillRect(left, top, width, height);
        g.setColor(borders);
        g.setStroke(stroke3);
        g.drawRect(left, top, width, height);
    }

    private void drawRemainingFill(Graphics2D g) {
    	int level = height * remainingDuration / duration;
    	Shape clip = g.getClip();
    	g.setClip(left,  top + (height - level), height, level);
        g.setColor(remaining);
        g.fillRect(left, top, width, height);
        g.setColor(borders);
        g.setStroke(stroke3);
        g.drawRect(left, top, width, height);
        g.setClip(clip);
    }

    private void drawRemainingText(Graphics2D g) {
        g.setColor(text);
        g.setFont(font);
        g.setStroke(stroke1);
        String s;
        if (remainingDuration >= 60) {
        	s = String.format("%d:%02d", remainingDuration / 60, remainingDuration % 60);
        } else {
        	s = String.format("%d", remainingDuration);
        }
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
        int height;
        switch (e.getKeyCode()) {
        case 107:
        case 49:
            height = frame.getHeight();
            height += 20;
            if (height > 2048) height = 2048;
            frame.setSize(width, height);
            prefs.putInt("height", frame.getHeight());
            break;
            
        case 109:
        case 45:
            height = frame.getWidth();
            height -= 20;
            if (height < 100) height= 100;
            frame.setSize(width, height);
            prefs.putInt("height", frame.getHeight());
            break;
            
        case 96:
        case 48:
            frame.setSize(width, 400);
            frame.setLocationRelativeTo(null);
            prefs.putInt("height", 400);
            prefs.remove("x");
            prefs.remove("y");
            break;

        // TODO handle duration increase/decrease
        case 10: // start/stop
        	if (started) {
        		started = false;
        	} else {
        		started = true;
        		if (startTime == -1L) startTime = System.currentTimeMillis();
        	}
        	break;
        	
        case 8: // reset
        	remainingDuration = duration;
        	startTime = -1;
        	started = false;
        	break;
        	
        case 84: // set test duration
        	if (started) break;
        	duration = 60;
        	remainingDuration = 60;
        	break;
        	
        case 33: // increase duration
        	if (duration == 60) {
        		duration = 0;
        		remainingDuration = 0;
        	}
        	duration += 5 * 60;
        	remainingDuration += 5 * 60;
            prefs.putInt("duration", duration);
        	break;
        	
        case 34: // decrease duration
        	if (duration > 5 * 60) {
        		duration -= 5 * 60;
        		remainingDuration -= 5 * 60;
        		if (remainingDuration < 0) {
        			remainingDuration = 0;
        		}
        	}
            prefs.putInt("duration", duration);
        	break;
            
        default:
            System.out.println(e.getKeyCode());
            break;
        }
    }

	public void updateValues() {
		if (started) {
			remainingDuration = Math.max(0,  duration - (int) ((System.currentTimeMillis() - startTime) / 1000L));
			if (remainingDuration == 0) {
				try {
					InputStream inputStream = getClass().getResourceAsStream("/gong.wav");
				    AudioStream audioStream = new AudioStream(inputStream);
				    AudioPlayer.player.start(audioStream);
				} catch (Exception e) {
					e.printStackTrace();
				}
				started = false;
				startTime = -1;
			}
		}
	}
    
}
