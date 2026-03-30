package prog.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Bottom status bar — dot indicator + animated mini progress bar. */
public class ProgressStatusBar extends JPanel {

    public enum State { IDLE, WORKING, SUCCESS, ERROR }

    private final JLabel      dot, msg;
    private final MiniBar     bar;
    private final JLabel      pct;

    public ProgressStatusBar() {
        super(new BorderLayout(8, 0));
        setBackground(Theme.BG_TITLEBAR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0, Theme.BORDER),
                new EmptyBorder(9, 20, 9, 20)));

        dot = new JLabel("●");
        dot.setFont(new Font("Arial", Font.PLAIN, 9));
        dot.setForeground(Theme.TEXT_TERTIARY);

        msg = new JLabel("Ready");
        msg.setFont(Theme.FONT_BODY);
        msg.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        left.setOpaque(false);
        left.add(dot); left.add(msg);

        bar = new MiniBar();
        bar.setPreferredSize(new Dimension(120, 4));

        pct = new JLabel("");
        pct.setFont(new Font("Consolas", Font.PLAIN, 10));
        pct.setForeground(Theme.TEXT_TERTIARY);
        pct.setPreferredSize(new Dimension(34, 14));
        bar.pctLabel = pct;

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(bar); right.add(pct);

        add(left,  BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    public void setStatus(State state, String message) {
        SwingUtilities.invokeLater(() -> {
            msg.setText(message);
            switch (state) {
                case IDLE:    dot.setForeground(Theme.TEXT_TERTIARY); break;
                case WORKING: dot.setForeground(Theme.BLUE);          break;
                case SUCCESS: dot.setForeground(Theme.GREEN);         break;
                case ERROR:   dot.setForeground(Theme.RED);           break;
            }
            bar.setState(state);
        });
    }

    public void setProgress(int percent) {
        SwingUtilities.invokeLater(() -> bar.setPercent(percent));
    }

    private static class MiniBar extends JComponent {
        private int   percent  = 0;
        private State state    = State.IDLE;
        JLabel pctLabel;
        private float indX = -0.4f;
        private Timer indTimer;

        void setState(State s) {
            state = s;
            if (s == State.WORKING) {
                percent = 0;
                if (pctLabel != null) pctLabel.setText("");
                if (indTimer != null) indTimer.stop();
                indX = -0.4f;
                indTimer = new Timer(16, e -> {
                    indX += 0.018f;
                    if (indX > 1.4f) indX = -0.4f;
                    repaint();
                });
                indTimer.start();
            } else {
                if (indTimer != null) { indTimer.stop(); indTimer = null; }
                if (s == State.SUCCESS) setPercent(100);
                else if (s == State.IDLE) setPercent(0);
            }
        }

        void setPercent(int p) {
            percent = Math.max(0, Math.min(100, p));
            if (pctLabel != null) pctLabel.setText(percent == 0 ? "" : percent + "%");
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(255,255,255,14));
            g2.fillRoundRect(0,0,w,h,h,h);
            if (state == State.WORKING) {
                int sw = (int)(w * 0.35f);
                int sx = (int)(indX * w) - sw/2;
                g2.setColor(Theme.BLUE);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,w,h,h,h));
                g2.fillRect(sx, 0, sw, h);
            } else if (percent > 0) {
                g2.setColor(state == State.ERROR ? Theme.RED : Theme.GREEN);
                g2.fillRoundRect(0,0,(int)(w*percent/100.0),h,h,h);
            }
            g2.dispose();
        }
    }
}
