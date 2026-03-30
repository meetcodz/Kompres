package prog.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel that paints a rounded-rectangle background and optional border.
 * Eliminates the need to override paintComponent in every custom panel.
 */
public class RoundedPanel extends JPanel {

    private Color  bgColor;
    private Color  borderColor;
    private int    radius;
    private float  borderWidth;

    public RoundedPanel(LayoutManager layout, Color bg, Color border,
                        int radius, float borderWidth) {
        super(layout);
        this.bgColor     = bg;
        this.borderColor = border;
        this.radius      = radius;
        this.borderWidth = borderWidth;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, Color bg) {
        this(layout, bg, null, Theme.R_MD, 0f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int r = radius * 2;

        if (bgColor != null) {
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, r, r);
        }
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            int off = (int) Math.ceil(borderWidth / 2);
            g2.drawRoundRect(off, off, w - off * 2, h - off * 2, r - off, r - off);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
