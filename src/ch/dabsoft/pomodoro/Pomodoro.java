package ch.dabsoft.pomodoro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Pomodoro extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    public Pomodoro() {
        super();
        setUndecorated(true);
        PomodoroComponent component = new PomodoroComponent(this); 
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new Ellipse2D.Double(0,0,getWidth(),getHeight()));
            }
        });
        Preferences prefs = Preferences.userNodeForPackage(Pomodoro.class);
        int size = prefs.getInt("size", 400);
        int x = prefs.getInt("x", -1);        
        int y = prefs.getInt("y", -1);
        component.duration = prefs.getInt("duration", 25);
        setSize(size, size);
        if (x == -1 || y == -1) {
            setLocationRelativeTo(null);
        } else {
            setLocation(x, y);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (prefs.getBoolean("always_on_top", true)) {
            setAlwaysOnTop(true);
        }
        add(component);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                component.keyPressed(e);
            }
        });
        Timer timer = new Timer(100, this);
        timer.start();
    }
    
    public static void main(String[] args) {
        new Pomodoro().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getContentPane().invalidate();
        getContentPane().repaint();
    }

    public void exitApp() {
        System.exit(0);
    }
}
