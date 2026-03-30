package prog.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Algorithm card — matches the mockup:
 *  - Colored 2-px top-edge strip (green for Huffman, amber for LZW)
 *  - Badge + title + description
 *  - Filled primary button + outlined secondary button
 *  - Smooth hover lift animation
 */
public class AlgorithmPanel extends JPanel {

    private final JButton compressButton;
    private final JButton decompressButton;
    private final Color   accentColor;
    private float         hoverT = 0f;
    private Timer         hoverTimer;

    public AlgorithmPanel(String algorithmName, String description,
                          String compressText,   Color compressColor,   String compressTip,
                          String decompressText, Color decompressColor, String decompressTip,
                          ActionListener listener) {
        super(new BorderLayout(0, 12));
        this.accentColor = compressColor;
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { animateHover(true);  }
            @Override public void mouseExited (MouseEvent e) { animateHover(false); }
        });

        // ── Header ────────────────────────────────────────────────────────────
        boolean isHuf = algorithmName.contains("Huffman");
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerRow.setOpaque(false);

        JLabel badge = buildBadge(isHuf ? "HUFFMAN" : "LZW", accentColor);
        JLabel name  = new JLabel(algorithmName);
        name.setFont(Theme.FONT_TITLE);
        name.setForeground(Theme.TEXT_PRIMARY);
        headerRow.add(badge);
        headerRow.add(name);

        // ── Description ───────────────────────────────────────────────────────
        JLabel desc = new JLabel("<html>" + description + "</html>");
        desc.setFont(Theme.FONT_SMALL);
        desc.setForeground(Theme.TEXT_TERTIARY);

        JPanel topBlock = new JPanel(new BorderLayout(0, 6));
        topBlock.setOpaque(false);
        topBlock.add(headerRow, BorderLayout.NORTH);
        topBlock.add(desc,      BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        compressButton   = filledButton(compressText,   compressColor);
        decompressButton = outlinedButton(decompressText);
        compressButton  .setToolTipText(compressTip);
        decompressButton.setToolTipText(decompressTip);
        compressButton  .addActionListener(listener);
        decompressButton.addActionListener(listener);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(compressButton);
        btnRow.add(decompressButton);

        add(topBlock, BorderLayout.CENTER);
        add(btnRow,   BorderLayout.SOUTH);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        int r = Theme.R_LG * 2;

        // Card background
        Color bg = blend(Theme.BG_SURFACE, Theme.BG_CARD, hoverT);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, r, r);

        // Border
        g2.setColor(blend(Theme.BORDER, Theme.BORDER2, hoverT));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w-1, h-1, r, r);

        // Top accent strip (2 px)
        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2.5f));
        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,w,h,r,r));
        g2.drawLine(Theme.R_LG, 1, w - Theme.R_LG, 1);

        g2.dispose();
        super.paintComponent(g);
    }

    public JButton getCompressButton()   { return compressButton;   }
    public JButton getDecompressButton() { return decompressButton; }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void animateHover(boolean in) {
        if (hoverTimer != null) hoverTimer.stop();
        hoverTimer = new Timer(12, e -> {
            hoverT += in ? 0.12f : -0.12f;
            if (in  && hoverT >= 1f) { hoverT = 1f; ((Timer)e.getSource()).stop(); }
            if (!in && hoverT <= 0f) { hoverT = 0f; ((Timer)e.getSource()).stop(); }
            repaint();
        });
        hoverTimer.start();
    }

    private static Color blend(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    private static JLabel buildBadge(String text, Color accent) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), 35);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.R_SM, Theme.R_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setOpaque(false);
        l.setForeground(accent);
        l.setFont(new Font("Consolas", Font.BOLD, 9));
        l.setBorder(new EmptyBorder(3, 8, 3, 8));
        return l;
    }

    private static JButton filledButton(String text, Color accent) {
        double lum = 0.299*accent.getRed() + 0.587*accent.getGreen() + 0.114*accent.getBlue();
        Color fg = lum > 140 ? new Color(15, 20, 15) : Theme.TEXT_PRIMARY;
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? accent.darker()
                         : getModel().isRollover() ? accent.brighter()
                         : accent;
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(), Theme.R_SM*2, Theme.R_SM*2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 12, 9, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton outlinedButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,18));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(), Theme.R_SM*2, Theme.R_SM*2);
                }
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1, Theme.R_SM*2, Theme.R_SM*2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_SMALL);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 12, 9, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
