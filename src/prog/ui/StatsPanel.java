package prog.ui;

import prog.model.CompressionStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Three metric cards + animated compression-ratio bar.
 * Monospaced numbers, green/red semantic colouring.
 */
public class StatsPanel extends JPanel {

    private final JLabel origVal, outVal, savedVal, ratioPct;
    private final RatioBar ratioBar;

    public StatsPanel() {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 20, 0, 20));

        // Metric cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);
        origVal  = monoLabel("—");
        outVal   = monoLabel("—");
        savedVal = monoLabel("—");
        cards.add(statCard("Original Size",  origVal));
        cards.add(statCard("Output Size",    outVal));
        cards.add(statCard("Space Saved",    savedVal));

        // Ratio bar section
        JPanel ratioSection = new JPanel(new BorderLayout(0, 6));
        ratioSection.setOpaque(false);
        JPanel ratioHeader = new JPanel(new BorderLayout());
        ratioHeader.setOpaque(false);
        JLabel ratioTitle = new JLabel("COMPRESSION RATIO");
        ratioTitle.setFont(Theme.FONT_CAPS);
        ratioTitle.setForeground(Theme.TEXT_TERTIARY);
        ratioPct = new JLabel("—");
        ratioPct.setFont(new Font("Consolas", Font.BOLD, 13));
        ratioPct.setForeground(Theme.TEXT_SECONDARY);
        ratioHeader.add(ratioTitle, BorderLayout.WEST);
        ratioHeader.add(ratioPct,   BorderLayout.EAST);
        ratioBar = new RatioBar();
        ratioBar.setPreferredSize(new Dimension(0, 6));
        ratioSection.add(ratioHeader, BorderLayout.NORTH);
        ratioSection.add(ratioBar,    BorderLayout.CENTER);

        add(cards,       BorderLayout.CENTER);
        add(ratioSection, BorderLayout.SOUTH);
    }

    // ── public API ────────────────────────────────────────────────────────────

    public void update(CompressionStats s) {
        origVal.setText(fmt(s.getOriginalBytes()));
        outVal .setText(fmt(s.getOutputBytes()));
        double pct = s.getRatioPercent();
        boolean pos = pct >= 0;
        savedVal.setText(pos ? String.format("%.1f%%", pct)
                            : String.format("+%.1f%%", Math.abs(pct)));
        savedVal.setForeground(pos ? Theme.GREEN : Theme.RED);
        outVal  .setForeground(pos ? Theme.GREEN : Theme.RED);
        ratioPct.setText(String.format("%.1f%%", pct));
        ratioPct.setForeground(pos ? Theme.GREEN : Theme.RED);
        ratioBar.setRatio((float)Math.min(Math.max(pct,-100),100), pos);
    }

    public void reset() {
        origVal.setText("—");  outVal.setText("—");  savedVal.setText("—");
        origVal.setForeground(Theme.TEXT_PRIMARY);
        outVal .setForeground(Theme.TEXT_PRIMARY);
        savedVal.setForeground(Theme.TEXT_PRIMARY);
        ratioPct.setText("—");  ratioPct.setForeground(Theme.TEXT_SECONDARY);
        ratioBar.setRatio(0f, true);
    }

    public void setOriginalSize(long bytes) {
        origVal.setText(fmt(bytes));
        outVal.setText("—");  savedVal.setText("—");
        outVal .setForeground(Theme.TEXT_PRIMARY);
        savedVal.setForeground(Theme.TEXT_PRIMARY);
        ratioPct.setText("—"); ratioBar.setRatio(0f, true);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private JPanel statCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int r = Theme.R_MD * 2;
                g2.setColor(Theme.BG_SURFACE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),r,r);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,r,r);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(13, 14, 13, 14));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(Theme.FONT_CAPS);
        lbl.setForeground(Theme.TEXT_TERTIARY);
        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel monoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_MONO);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private static String fmt(long b) {
        if (b < 1024)        return b + " B";
        if (b < 1_048_576)   return String.format("%.1f KB", b/1024.0);
        if (b < 1_073_741_824L) return String.format("%.2f MB", b/1_048_576.0);
        return String.format("%.2f GB", b/1_073_741_824.0);
    }

    // ── Animated ratio bar ────────────────────────────────────────────────────

    private static class RatioBar extends JComponent {
        private float target = 0, display = 0;
        private boolean positive = true;
        private Timer anim;

        void setRatio(float r, boolean pos) {
            target = Math.abs(r); positive = pos;
            if (anim != null && anim.isRunning()) anim.stop();
            anim = new Timer(12, e -> {
                float d = target - display;
                if (Math.abs(d) < 0.4f) { display = target; ((Timer)e.getSource()).stop(); }
                else display += d * 0.13f;
                repaint();
            });
            anim.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(255,255,255,14));
            g2.fillRoundRect(0,0,w,h,h,h);
            int filled = (int)(w * display / 100f);
            if (filled > 0) {
                g2.setColor(positive ? Theme.GREEN : Theme.RED);
                g2.fillRoundRect(0,0,filled,h,h,h);
            }
            g2.dispose();
        }
    }
}
