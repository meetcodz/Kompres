package prog.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Drag-and-drop file drop zone — premium dark Fluent style.
 * Animated border glow on drag-over, green-tinted when a file is selected.
 */
public class DropZonePanel extends JPanel {

    private final JTextField   filePathField;
    private final JButton      browseButton;
    private boolean            isDragOver   = false;
    private boolean            hasFile      = false;
    private float              glowAlpha    = 0f;
    private Timer              glowTimer;

    public DropZonePanel(ActionListener browseListener, Consumer<File> onFileDrop) {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 4, 20));

        // ── inner card ────────────────────────────────────────────────────────
        JPanel inner = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Fill
                Color fill = isDragOver
                        ? new Color(52, 199, 89, 22)
                        : hasFile
                            ? new Color(26, 43, 29)
                            : Theme.BG_SURFACE;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, Theme.R_LG * 2, Theme.R_LG * 2);

                // Glow
                if (glowAlpha > 0) {
                    g2.setColor(new Color(52, 199, 89, (int)(glowAlpha * 70)));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, w-2, h-2, Theme.R_LG*2-1, Theme.R_LG*2-1);
                }

                // Dashed border
                float[] dash = {5f, 4f};
                Color borderCol = isDragOver || hasFile
                        ? new Color(52, 199, 89, 160)
                        : new Color(255, 255, 255, 28);
                g2.setColor(borderCol);
                g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10f, dash, 0f));
                g2.drawRoundRect(1, 1, w-2, h-2, Theme.R_LG*2-1, Theme.R_LG*2-1);
                g2.dispose();
            }
        };
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(18, 18, 18, 14));

        // Upload icon blob
        JLabel iconBlob = new JLabel(buildUploadIcon());
        iconBlob.setPreferredSize(new Dimension(52, 52));

        // Text
        JPanel textBlock = new JPanel(new BorderLayout(0, 4));
        textBlock.setOpaque(false);
        JLabel title = new JLabel("Drop a file here to compress or decompress");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        filePathField = new JTextField("or click Browse to select any file type");
        filePathField.setEditable(false);
        filePathField.setFont(Theme.FONT_SMALL);
        filePathField.setForeground(Theme.TEXT_TERTIARY);
        filePathField.setBorder(null);
        filePathField.setOpaque(false);
        textBlock.add(title,         BorderLayout.NORTH);
        textBlock.add(filePathField, BorderLayout.SOUTH);

        // Browse button
        browseButton = new JButton("  Browse…  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(255,255,255,30) : new Color(255,255,255,15);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(new Color(255,255,255,46));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        browseButton.setFont(Theme.FONT_BODY);
        browseButton.setForeground(Theme.TEXT_PRIMARY);
        browseButton.setFocusPainted(false);
        browseButton.setContentAreaFilled(false);
        browseButton.setOpaque(false);
        browseButton.setBorder(new EmptyBorder(7, 14, 7, 14));
        browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseButton.addActionListener(browseListener);

        inner.add(iconBlob,    BorderLayout.WEST);
        inner.add(textBlock,   BorderLayout.CENTER);
        inner.add(browseButton, BorderLayout.EAST);
        add(inner, BorderLayout.CENTER);

        // ── Drag & Drop ───────────────────────────────────────────────────────
        new DropTarget(inner, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent e) {
                if (isFileDrag(e.getTransferable())) { isDragOver = true; animateGlow(true); }
            }
            @Override public void dragExit(DropTargetEvent e) {
                isDragOver = false; animateGlow(false);
            }
            @Override public void drop(DropTargetDropEvent e) {
                isDragOver = false; animateGlow(false);
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>)
                            e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && onFileDrop != null) onFileDrop.accept(files.get(0));
                    e.dropComplete(true);
                } catch (Exception ex) { e.dropComplete(false); }
            }
        });
    }

    public void setSelectedFile(File file) {
        hasFile = (file != null);
        if (file == null) {
            filePathField.setText("or click Browse to select any file type");
            filePathField.setForeground(Theme.TEXT_TERTIARY);
        } else {
            filePathField.setText(file.getAbsolutePath());
            filePathField.setForeground(Theme.GREEN);
        }
        repaint();
    }

    public JTextField getFilePathField() { return filePathField; }
    public JButton    getBrowseButton()  { return browseButton; }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void animateGlow(boolean fadeIn) {
        if (glowTimer != null) glowTimer.stop();
        glowTimer = new Timer(14, e -> {
            glowAlpha += fadeIn ? 0.1f : -0.1f;
            if (fadeIn  && glowAlpha >= 1f) { glowAlpha = 1f; ((Timer)e.getSource()).stop(); }
            if (!fadeIn && glowAlpha <= 0f) { glowAlpha = 0f; ((Timer)e.getSource()).stop(); }
            repaint();
        });
        glowTimer.start();
    }

    private static boolean isFileDrag(Transferable t) {
        return t.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    private static Icon buildUploadIcon() {
        return new Icon() {
            public int getIconWidth()  { return 52; }
            public int getIconHeight() { return 52; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Circle bg
                g2.setColor(Theme.GREEN_DIM);
                g2.fillOval(x, y, 52, 52);
                // Arrow
                g2.setColor(Theme.GREEN);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                int cx = x + 26, cy = y + 26;
                g2.drawLine(cx, cy - 9, cx, cy + 7);
                g2.drawLine(cx - 7, cy - 2, cx, cy - 9);
                g2.drawLine(cx + 7, cy - 2, cx, cy - 9);
                g2.drawLine(cx - 8, cy + 10, cx + 8, cy + 10);
                g2.dispose();
            }
        };
    }
}
