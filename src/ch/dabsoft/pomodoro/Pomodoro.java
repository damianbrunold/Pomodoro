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

    private PomodoroComponent component;
    
    public Pomodoro() {
        super();
        setUndecorated(true);
        component = new PomodoroComponent(this); 
        Preferences prefs = Preferences.userNodeForPackage(Pomodoro.class);
        int width = prefs.getInt("width", 100);
        int height = prefs.getInt("height", 400);
        int x = prefs.getInt("x", -1);        
        int y = prefs.getInt("y", -1);
        component.duration = prefs.getInt("duration", 25 * 60);
        component.remainingDuration = component.duration;
        setSize(width, height);
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
		component.updateValues();
        getContentPane().invalidate();
        getContentPane().repaint();
    }

    public void exitApp() {
        System.exit(0);
    }
}
