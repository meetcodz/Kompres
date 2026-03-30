package prog.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/** Compact output-folder selector bar — dark themed. */
public class OutputDirPanel extends JPanel {

    private File    outputDir;
    private final JLabel pathLabel;

    public OutputDirPanel() {
        super(new BorderLayout(10, 0));
        setBackground(Theme.BG_SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(9, 14, 9, 10)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(new JLabel(folderIcon()));
        JLabel tag = new JLabel("OUTPUT");
        tag.setFont(Theme.FONT_CAPS);
        tag.setForeground(Theme.TEXT_TERTIARY);
        left.add(tag);

        pathLabel = new JLabel(getDefaultDir().getAbsolutePath());
        pathLabel.setFont(Theme.FONT_SMALL);
        pathLabel.setForeground(Theme.TEXT_SECONDARY);

        JButton changeBtn = new JButton("Change") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? new Color(10,132,255,40) : new Color(10,132,255,20);
                g2.setColor(bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(10,132,255,120));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        changeBtn.setFont(Theme.FONT_TINY);
        changeBtn.setForeground(Theme.BLUE);
        changeBtn.setFocusPainted(false);
        changeBtn.setContentAreaFilled(false);
        changeBtn.setOpaque(false);
        changeBtn.setBorder(new EmptyBorder(4, 12, 4, 12));
        changeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changeBtn.addActionListener(e -> chooseDir());

        outputDir = getDefaultDir();
        add(left,      BorderLayout.WEST);
        add(pathLabel, BorderLayout.CENTER);
        add(changeBtn, BorderLayout.EAST);
    }

    public File getOutputDir() { return outputDir; }

    public void syncToFile(File f) {
        if (f != null && f.getParentFile() != null) {
            outputDir = f.getParentFile();
            pathLabel.setText(outputDir.getAbsolutePath());
        }
    }

    private void chooseDir() {
        JFileChooser fc = new JFileChooser(outputDir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select output folder");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputDir = fc.getSelectedFile();
            pathLabel.setText(outputDir.getAbsolutePath());
        }
    }

    private static File getDefaultDir() {
        return new File(System.getProperty("user.home"));
    }

    private static Icon folderIcon() {
        return new Icon() {
            public int getIconWidth()  { return 16; }
            public int getIconHeight() { return 16; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.TEXT_TERTIARY);
                g2.fillRoundRect(x, y+5, 14, 9, 3, 3);
                g2.fillRoundRect(x, y+3, 6, 4, 2, 2);
                g2.dispose();
            }
        };
    }
}
