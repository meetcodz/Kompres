package prog.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/** Creates and shows the main application frame — dark Fluent theme. */
public final class MainWindow {
    private MainWindow() {}

    public static void init(JPanel contentPane, ActionListener openFileListener) {
        SwingUtilities.invokeLater(() -> {
            patchUIDefaults();

            JFrame frame = new JFrame("Kompres — File Compressor");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setBackground(Theme.BG_APP);
            frame.setContentPane(contentPane);

            // Minimal dark menu bar
            JMenuBar bar = new JMenuBar();
            bar.setBackground(Theme.BG_TITLEBAR);
            bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
            bar.setOpaque(true);

            JMenu fileMenu = styledMenu("File");
            JMenuItem openItem = styledMenuItem("Open File…");
            openItem.addActionListener(openFileListener);
            fileMenu.add(openItem);

            JMenuItem exitItem = styledMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            fileMenu.add(new JSeparator());
            fileMenu.add(exitItem);
            bar.add(fileMenu);

            JMenu helpMenu = styledMenu("Help");
            helpMenu.add(styledMenuItem("About Kompres"));
            bar.add(helpMenu);

            frame.setJMenuBar(bar);
            frame.setMinimumSize(new Dimension(1020, 700));
            frame.setSize(1180, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JMenu styledMenu(String text) {
        JMenu m = new JMenu(text);
        m.setFont(Theme.FONT_BODY);
        m.setForeground(Theme.TEXT_SECONDARY);
        return m;
    }

    private static JMenuItem styledMenuItem(String text) {
        JMenuItem i = new JMenuItem(text);
        i.setFont(Theme.FONT_BODY);
        i.setBackground(Theme.BG_SURFACE);
        i.setForeground(Theme.TEXT_PRIMARY);
        return i;
    }

    private static void patchUIDefaults() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        Object[][] patches = {
            {"OptionPane.background",        Theme.BG_SURFACE},
            {"Panel.background",             Theme.BG_SURFACE},
            {"OptionPane.messageForeground", Theme.TEXT_PRIMARY},
            {"Button.background",            Theme.BG_CARD},
            {"Button.foreground",            Theme.TEXT_PRIMARY},
            {"Label.foreground",             Theme.TEXT_PRIMARY},
            {"TextField.background",         Theme.BG_SURFACE},
            {"TextField.foreground",         Theme.TEXT_PRIMARY},
            {"TextField.caretForeground",    Theme.GREEN},
            {"List.background",              Theme.BG_CARD},
            {"List.foreground",              Theme.TEXT_PRIMARY},
            {"ScrollPane.background",        Theme.BG_SURFACE},
            {"ScrollBar.background",         Theme.BG_SURFACE},
            {"ScrollBar.thumb",              Theme.BORDER},
            {"Separator.foreground",         Theme.BORDER},
            {"MenuBar.background",           Theme.BG_TITLEBAR},
            {"Menu.background",              Theme.BG_SURFACE},
            {"Menu.foreground",              Theme.TEXT_SECONDARY},
            {"MenuItem.background",          Theme.BG_SURFACE},
            {"MenuItem.foreground",          Theme.TEXT_PRIMARY},
            {"PopupMenu.background",         Theme.BG_SURFACE},
            {"PopupMenu.border",             BorderFactory.createLineBorder(Theme.BORDER2, 1)},
            {"FileChooser.background",       Theme.BG_SURFACE},
            {"ComboBox.background",          Theme.BG_CARD},
            {"Table.background",             Theme.BG_SURFACE},
            {"Table.foreground",             Theme.TEXT_PRIMARY},
            {"TableHeader.background",       Theme.BG_CARD},
            {"TableHeader.foreground",       Theme.TEXT_SECONDARY},
            {"ToolTip.background",           Theme.BG_CARD},
            {"ToolTip.foreground",           Theme.TEXT_PRIMARY},
            {"ToolTip.border",               BorderFactory.createLineBorder(Theme.BORDER2, 1)},
        };
        for (Object[] p : patches) UIManager.put(p[0], p[1]);
    }
}
