package prog.ui;

import prog.model.RecentFilesManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Consumer;

/**
 * Root UI panel — assembles sidebar + main content area + status bar.
 *
 * Layout:
 *  ┌──────────────┬──────────────────────────────────────────┐
 *  │ SidebarPanel │  scroll → DropZonePanel                  │
 *  │  248 px      │           OutputDirPanel                 │
 *  │              │           AlgorithmPanel × 2 (grid 1×2) │
 *  │              │           StatsPanel                     │
 *  ├──────────────┴──────────────────────────────────────────┤
 *  │ ProgressStatusBar                                       │
 *  └─────────────────────────────────────────────────────────┘
 */
public class FileCompressorUI {

    private DropZonePanel     dropZone;
    private OutputDirPanel    outputDir;
    private StatsPanel        stats;
    private ProgressStatusBar statusBar;
    private SidebarPanel      sidebar;

    private JButton huffmanCompressBtn, huffmanDecompressBtn;
    private JButton lzwCompressBtn,     lzwDecompressBtn;
    private JButton exitBtn;

    // ── Getters for Main ──────────────────────────────────────────────────────
    public JTextField        getFilePathField()           { return dropZone.getFilePathField(); }
    public JButton           getBrowseButton()            { return dropZone.getBrowseButton(); }
    public JButton           getHuffmanCompressButton()   { return huffmanCompressBtn; }
    public JButton           getHuffmanDecompressButton() { return huffmanDecompressBtn; }
    public JButton           getLzwCompressButton()       { return lzwCompressBtn; }
    public JButton           getLzwDecompressButton()     { return lzwDecompressBtn; }
    public JButton           getExitButton()              { return exitBtn; }
    public StatsPanel        getStatsPanel()              { return stats; }
    public ProgressStatusBar getProgressBar()             { return statusBar; }
    public OutputDirPanel    getOutputDirPanel()          { return outputDir; }

    // ── Build ─────────────────────────────────────────────────────────────────

    public JPanel createContentPane(ActionListener buttonListener,
                                    ActionListener browseListener,
                                    RecentFilesManager recentMgr,
                                    Consumer<File> onFileDrop,
                                    Consumer<File> onRecentClicked) {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_APP);

        // Sidebar
        sidebar = new SidebarPanel(recentMgr, onRecentClicked);
        root.add(sidebar, BorderLayout.WEST);

        // Main column
        JPanel mainOuter = new JPanel(new BorderLayout());
        mainOuter.setBackground(Theme.BG_APP);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(Theme.BG_APP);
        mainContent.setBorder(new EmptyBorder(0, 0, 20, 0));

        dropZone  = new DropZonePanel(browseListener, onFileDrop);
        outputDir = new OutputDirPanel();
        stats     = new StatsPanel();

        // Output dir wrapper
        JPanel outWrap = new JPanel(new BorderLayout());
        outWrap.setOpaque(false);
        outWrap.setBorder(new EmptyBorder(6, 20, 0, 20));
        outWrap.add(outputDir, BorderLayout.CENTER);

        mainContent.add(dropZone);
        mainContent.add(outWrap);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(buildAlgoSection(buttonListener));
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(buildStatsSection());

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BG_APP);
        scroll.getVerticalScrollBar().setBackground(Theme.BG_APP);

        mainOuter.add(scroll, BorderLayout.CENTER);

        statusBar = new ProgressStatusBar();
        mainOuter.add(statusBar, BorderLayout.SOUTH);

        root.add(mainOuter, BorderLayout.CENTER);
        return root;
    }

    // ── Algorithm section ────────────────────────────────────────────────────

    private JPanel buildAlgoSection(ActionListener listener) {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 20, 0, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("CHOOSE ALGORITHM");
        title.setFont(Theme.FONT_CAPS);
        title.setForeground(Theme.TEXT_TERTIARY);
        header.add(title, BorderLayout.WEST);

        // Exit link (unobtrusive)
        exitBtn = new JButton("Exit");
        exitBtn.setFont(Theme.FONT_TINY);
        exitBtn.setForeground(new Color(Theme.RED.getRed(), Theme.RED.getGreen(),
                Theme.RED.getBlue(), 140));
        exitBtn.setFocusPainted(false);
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        exitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(listener);
        header.add(exitBtn, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setOpaque(false);

        AlgorithmPanel huffPanel = new AlgorithmPanel(
                "Huffman Coding",
                "Frequency-based compression. Best for text files, source code, logs and any uncompressed binary.",
                "Compress",   Theme.GREEN, "Compress with Huffman coding",
                "Decompress", Theme.BLUE,  "Decompress a .huffz file",
                listener);
        huffmanCompressBtn   = huffPanel.getCompressButton();
        huffmanDecompressBtn = huffPanel.getDecompressButton();

        AlgorithmPanel lzwPanel = new AlgorithmPanel(
                "LZW Algorithm",
                "Dictionary-based compression. Best for XML, JSON, CSV, HTML and highly repetitive structured data.",
                "Compress",   Theme.AMBER, "Compress with LZW algorithm",
                "Decompress", Theme.BLUE,  "Decompress a .LmZWp file",
                listener);
        lzwCompressBtn   = lzwPanel.getCompressButton();
        lzwDecompressBtn = lzwPanel.getDecompressButton();

        grid.add(huffPanel);
        grid.add(lzwPanel);

        wrap.add(header, BorderLayout.NORTH);
        wrap.add(grid,   BorderLayout.CENTER);
        return wrap;
    }

    // ── Stats section ────────────────────────────────────────────────────────

    private JPanel buildStatsSection() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("COMPRESSION STATISTICS");
        title.setFont(Theme.FONT_CAPS);
        title.setForeground(Theme.TEXT_TERTIARY);

        // stats panel has its own internal border, remove it and wrap here
        stats = new StatsPanel();

        wrap.add(title, BorderLayout.NORTH);
        wrap.add(stats, BorderLayout.CENTER);
        return wrap;
    }

    // ── Public updaters ──────────────────────────────────────────────────────

    public void refreshSidebar() {
        if (sidebar != null) sidebar.refreshRecentList();
    }

    public void setSelectedFile(File file) {
        if (dropZone  != null) dropZone.setSelectedFile(file);
        if (outputDir != null) outputDir.syncToFile(file);
    }
}
