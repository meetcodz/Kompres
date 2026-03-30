package prog.ui;

import prog.model.FileType;
import prog.model.RecentFile;
import prog.model.RecentFilesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left sidebar — brand header, nav items, recent files, storage summary.
 * Matches the interactive mockup exactly.
 */
public class SidebarPanel extends JPanel {

    private final RecentFilesManager recentMgr;
    private final Consumer<File>     onRecentClick;
    private JPanel                   recentList;

    public SidebarPanel(RecentFilesManager recentMgr, Consumer<File> onRecentClick) {
        super(new BorderLayout());
        this.recentMgr    = recentMgr;
        this.onRecentClick = onRecentClick;

        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(248, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Stack: top block + scrollable mid + bottom storage bar
        JPanel top = buildTop();
        JScrollPane mid = buildMid();
        JPanel bot = buildBottom();

        add(top, BorderLayout.NORTH);
        add(mid, BorderLayout.CENTER);
        add(bot, BorderLayout.SOUTH);
    }

    // ── Brand header ─────────────────────────────────────────────────────────

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_SIDEBAR);
        p.setBorder(new EmptyBorder(16, 14, 10, 14));

        // Logo + name
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setBackground(Theme.BG_SIDEBAR);
        brand.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel logoIcon = new JLabel(buildLogoIcon());
        JPanel nameBlock = new JPanel(new BorderLayout(0, 1));
        nameBlock.setBackground(Theme.BG_SIDEBAR);
        JLabel appName = new JLabel("Kompres");
        appName.setFont(Theme.FONT_BRAND);
        appName.setForeground(Theme.TEXT_PRIMARY);
        JLabel appSub = new JLabel("FILE COMPRESSOR");
        appSub.setFont(new Font("Segoe UI", Font.BOLD, 9));
        appSub.setForeground(Theme.TEXT_TERTIARY);
        nameBlock.add(appName, BorderLayout.CENTER);
        nameBlock.add(appSub,  BorderLayout.SOUTH);
        brand.add(logoIcon);
        brand.add(nameBlock);

        // Nav items
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.BG_SIDEBAR);
        nav.add(capsLabel("Menu"));
        nav.add(Box.createVerticalStrut(4));
        nav.add(navItem("Compress / Decompress", buildGridIcon(), true));
        nav.add(Box.createVerticalStrut(2));
        nav.add(navItem("Recent History",         buildListIcon(), false));
        nav.add(Box.createVerticalStrut(2));
        nav.add(navItem("About",                  buildInfoIcon(), false));

        p.add(brand, BorderLayout.NORTH);
        p.add(nav,   BorderLayout.CENTER);
        return p;
    }

    private JPanel navItem(String label, Icon icon, boolean active) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)) {
            @Override protected void paintComponent(Graphics g) {
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.GREEN_GLOW);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                            Theme.R_SM * 2, Theme.R_SM * 2);
                    g2.setColor(new Color(Theme.GREEN.getRed(), Theme.GREEN.getGreen(),
                            Theme.GREEN.getBlue(), 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1,
                            Theme.R_SM * 2, Theme.R_SM * 2);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(Theme.FONT_SMALL);
        textLbl.setForeground(active ? Theme.GREEN : Theme.TEXT_SECONDARY);

        row.add(iconLbl);
        row.add(textLbl);

        if (!active) {
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    row.setBackground(Theme.BG_HOVER);
                    row.setOpaque(true); row.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    row.setOpaque(false); row.repaint();
                }
            });
        }
        return row;
    }

    // ── Scrollable recent-files section ───────────────────────────────────────

    private JScrollPane buildMid() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_SIDEBAR);
        content.setBorder(new EmptyBorder(8, 14, 8, 14));

        content.add(capsLabel("Recent Files"));
        content.add(Box.createVerticalStrut(6));

        recentList = new JPanel();
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setBackground(Theme.BG_SIDEBAR);
        refreshRecentList();
        content.add(recentList);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BG_SIDEBAR);
        scroll.getVerticalScrollBar().setBackground(Theme.BG_SIDEBAR);
        return scroll;
    }

    public void refreshRecentList() {
        recentList.removeAll();
        List<RecentFile> entries = recentMgr.getEntries();
        if (entries.isEmpty()) {
            JLabel empty = new JLabel("No recent files");
            empty.setFont(Theme.FONT_SMALL);
            empty.setForeground(Theme.TEXT_TERTIARY);
            empty.setBorder(new EmptyBorder(4, 4, 4, 4));
            recentList.add(empty);
        } else {
            for (RecentFile rf : entries) {
                recentList.add(recentRow(rf));
                recentList.add(Box.createVerticalStrut(1));
            }
        }
        recentList.revalidate();
        recentList.repaint();
    }

    private JPanel recentRow(RecentFile rf) {
        JPanel row = new JPanel(new BorderLayout(9, 0));
        row.setBackground(Theme.BG_SIDEBAR);
        row.setBorder(new EmptyBorder(7, 8, 7, 8));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        row.add(buildBadge(rf), BorderLayout.WEST);

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setBackground(Theme.BG_SIDEBAR);
        JLabel name = new JLabel(rf.getName());
        name.setFont(Theme.FONT_SMALL);
        name.setForeground(Theme.TEXT_PRIMARY);
        JLabel meta = new JLabel(fmtSize(rf.getSizeBytes()) + "  ·  " + rf.getRelativeTime());
        meta.setFont(Theme.FONT_TINY);
        meta.setForeground(Theme.TEXT_TERTIARY);
        info.add(name, BorderLayout.NORTH);
        info.add(meta, BorderLayout.SOUTH);
        row.add(info, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(255,255,255,10));
                info.setBackground(new Color(255,255,255,10));
            }
            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(Theme.BG_SIDEBAR);
                info.setBackground(Theme.BG_SIDEBAR);
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (onRecentClick != null)
                    onRecentClick.accept(new File(rf.getAbsolutePath()));
            }
        });
        return row;
    }

    private JLabel buildBadge(RecentFile rf) {
        Color bg, fg;
        switch (rf.getFileType()) {
            case HUFFMAN:         bg = Theme.GREEN_DIM;  fg = Theme.GREEN; break;
            case LZW:             bg = Theme.AMBER_DIM;  fg = Theme.AMBER; break;
            case VIDEO:
            case AUDIO_LOSSY:
            case AUDIO_LOSSLESS: bg = new Color(80,0,80); fg = new Color(220,130,255); break;
            default:              bg = Theme.BLUE_DIM;   fg = Theme.BLUE;  break;
        }
        JLabel badge = new JLabel(rf.getBadgeLabel(), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        Theme.R_SM, Theme.R_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setFont(new Font("Consolas", Font.BOLD, 8));
        badge.setPreferredSize(new Dimension(30, 22));
        return badge;
    }

    // ── Storage summary at bottom ─────────────────────────────────────────────

    private JPanel buildBottom() {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Theme.BG_SIDEBAR);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(12, 14, 14, 14)));

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.BG_SIDEBAR);
        JLabel lbl = new JLabel("Storage saved");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        JLabel val = new JLabel("— KB");
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(Theme.GREEN);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        // Static bar (decorative — would be wired to real stats in production)
        JPanel trackWrap = new JPanel(new BorderLayout());
        trackWrap.setBackground(new Color(255,255,255,14));
        trackWrap.setPreferredSize(new Dimension(0, 4));
        trackWrap.setBorder(null);
        JPanel fill = new JPanel();
        fill.setBackground(Theme.GREEN);
        fill.setPreferredSize(new Dimension(0, 4));

        p.add(row,      BorderLayout.NORTH);
        p.add(trackWrap, BorderLayout.CENTER);
        JLabel hint = new JLabel("Avg compression across all files");
        hint.setFont(Theme.FONT_TINY);
        hint.setForeground(Theme.TEXT_TERTIARY);
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    // ── Icon factories ────────────────────────────────────────────────────────

    private static Icon buildLogoIcon() {
        return new Icon() {
            public int getIconWidth()  { return 32; }
            public int getIconHeight() { return 32; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Green rounded square
                GradientPaint gp = new GradientPaint(x, y, Theme.GREEN,
                        x+32, y+32, new Color(48, 209, 88));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, 32, 32, 10, 10);
                // Arrow icon
                g2.setColor(Theme.GREEN_DARK_TXT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawLine(x+16, x+8, x+16, y+20);
                g2.drawLine(x+10, y+14, x+16, y+8);
                g2.drawLine(x+22, y+14, x+16, y+8);
                g2.drawLine(x+9,  y+23, x+23, y+23);
                g2.dispose();
            }
        };
    }

    private static Icon buildGridIcon() {
        return tinyIcon(g2 -> {
            g2.fillRoundRect(0, 0, 6, 6, 2, 2);
            g2.fillRoundRect(8, 0, 6, 6, 2, 2);
            g2.fillRoundRect(0, 8, 6, 6, 2, 2);
            g2.fillRoundRect(8, 8, 6, 6, 2, 2);
        });
    }

    private static Icon buildListIcon() {
        return tinyIcon(g2 -> {
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(0, 3, 14, 3);
            g2.drawLine(0, 7, 10, 7);
            g2.drawLine(0, 11, 12, 11);
        });
    }

    private static Icon buildInfoIcon() {
        return tinyIcon(g2 -> {
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(0, 0, 14, 14);
            g2.drawLine(7, 6, 7, 10);
            g2.fillOval(6, 3, 2, 2);
        });
    }

    @FunctionalInterface interface Painter { void paint(Graphics2D g2); }

    private static Icon tinyIcon(Painter p) {
        return new Icon() {
            public int getIconWidth()  { return 16; }
            public int getIconHeight() { return 16; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x + 1, y + 1);
                g2.setColor(Theme.TEXT_TERTIARY);
                p.paint(g2);
                g2.dispose();
            }
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JLabel capsLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(Theme.TEXT_TERTIARY);
        l.setBorder(new EmptyBorder(0, 2, 0, 0));
        return l;
    }

    private static String fmtSize(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1_048_576)   return String.format("%.1f KB", bytes/1024.0);
        if (bytes < 1_073_741_824L) return String.format("%.2f MB", bytes/1_048_576.0);
        return String.format("%.2f GB", bytes/1_073_741_824.0);
    }
}
