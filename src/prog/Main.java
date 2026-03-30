package prog;

import prog.handler.CompressionHandler;
import prog.handler.DecompressionHandler;
import prog.handler.FileOperationHandler;
import prog.model.CompressionStats;
import prog.model.FileType;
import prog.model.RecentFilesManager;
import prog.ui.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Application controller — wires UI events to business-logic handlers.
 *
 * v4.0 changes:
 *  - Real Huffman (bit-level) and LZW (12-bit dictionary) algorithms
 *  - Smart pre-check warns on JPEG / MP4 / ZIP etc. before compressing
 *  - Progress callback wires the status bar to real byte-write progress
 *  - Background SwingWorker keeps the UI responsive on large files
 *  - Fluent dark-theme UI matching the interactive mockup
 */
public class Main implements ActionListener {

    // ── Handlers ──────────────────────────────────────────────────────────────
    private final FileOperationHandler  fileHandler;
    private final CompressionHandler    compressionHandler;
    private final DecompressionHandler  decompressionHandler;
    private final RecentFilesManager    recentMgr;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final FileCompressorUI ui;

    // ── Button refs (cached after createContentPane) ──────────────────────────
    private JButton huffmanCompressBtn, huffmanDecompressBtn;
    private JButton lzwCompressBtn,     lzwDecompressBtn;
    private JButton exitBtn;

    public Main() {
        ui                   = new FileCompressorUI();
        fileHandler          = new FileOperationHandler();
        compressionHandler   = new CompressionHandler();
        decompressionHandler = new DecompressionHandler();
        recentMgr            = new RecentFilesManager();
    }

    // ── Content-pane factory ─────────────────────────────────────────────────

    public JPanel createContentPane() {
        JPanel pane = ui.createContentPane(
                this,
                this::handleBrowse,
                recentMgr,
                this::handleFileDrop,
                this::handleRecentClick);

        huffmanCompressBtn   = ui.getHuffmanCompressButton();
        huffmanDecompressBtn = ui.getHuffmanDecompressButton();
        lzwCompressBtn       = ui.getLzwCompressButton();
        lzwDecompressBtn     = ui.getLzwDecompressButton();
        exitBtn              = ui.getExitButton();
        return pane;
    }

    // ── File selection ────────────────────────────────────────────────────────

    private void handleBrowse(ActionEvent e) {
        File f = fileHandler.browseForFile();
        if (f != null) applyFile(f);
    }

    private void handleFileDrop(File f) {
        fileHandler.setSelectedFile(f);
        applyFile(f);
    }

    private void handleRecentClick(File f) {
        if (f.exists()) {
            fileHandler.setSelectedFile(f);
            applyFile(f);
        } else {
            ui.getProgressBar().setStatus(ProgressStatusBar.State.ERROR,
                    "File no longer exists: " + f.getName());
        }
    }

    private void applyFile(File f) {
        fileHandler.setSelectedFile(f);
        ui.setSelectedFile(f);
        ui.getStatsPanel().setOriginalSize(f.length());
        ui.getProgressBar().setStatus(ProgressStatusBar.State.IDLE,
                "Selected: " + f.getName() + "  (" + fmt(f.length()) + ")");
        recentMgr.add(f);
        ui.refreshSidebar();

        // Advisory hint for already-compressed formats
        FileType ft = FileType.detect(f.getName());
        if (ft.isAlreadyCompressed() && ft != FileType.HUFFMAN && ft != FileType.LZW) {
            ui.getProgressBar().setStatus(ProgressStatusBar.State.ERROR,
                    "⚠  " + f.getName() + " is already compressed — gains will be minimal");
        }
    }

    private void handleFileOpen() { handleBrowse(null); }

    // ── Action dispatcher ─────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if      (src == huffmanCompressBtn)   runOp(this::doHuffmanCompress);
        else if (src == huffmanDecompressBtn) runOp(this::doHuffmanDecompress);
        else if (src == lzwCompressBtn)       runOp(this::doLzwCompress);
        else if (src == lzwDecompressBtn)     runOp(this::doLzwDecompress);
        else if (src == exitBtn)              System.exit(0);
    }

    /**
     * Runs an operation on a background SwingWorker.
     * Disables all buttons and shows the animated progress bar while running.
     */
    private void runOp(Runnable op) {
        if (!fileHandler.isFileSelected()) {
            fileHandler.showNoFileSelectedWarning();
            ui.getProgressBar().setStatus(ProgressStatusBar.State.ERROR,
                    "No file selected");
            return;
        }
        setButtonsEnabled(false);
        ui.getProgressBar().setStatus(ProgressStatusBar.State.WORKING, "Working…");

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() { op.run(); return null; }
            @Override protected void done()           { setButtonsEnabled(true); }
        };
        w.execute();
    }

    // ── Concrete operations ───────────────────────────────────────────────────

    private void doHuffmanCompress() {
        File in  = fileHandler.getSelectedFile();
        File out = compressionHandler.compressWithHuffman(in,
                ui.getOutputDirPanel().getOutputDir(),
                bytes -> reportProgress(bytes, in.length()));
        handleResult(in, out, true);
    }

    private void doHuffmanDecompress() {
        File in  = fileHandler.getSelectedFile();
        File out = decompressionHandler.decompressHuffman(in,
                ui.getOutputDirPanel().getOutputDir(),
                bytes -> reportProgress(bytes, in.length()));
        handleResult(in, out, false);
    }

    private void doLzwCompress() {
        File in  = fileHandler.getSelectedFile();
        File out = compressionHandler.compressWithLZW(in,
                ui.getOutputDirPanel().getOutputDir(),
                bytes -> reportProgress(bytes, in.length()));
        handleResult(in, out, true);
    }

    private void doLzwDecompress() {
        File in  = fileHandler.getSelectedFile();
        File out = decompressionHandler.decompressLZW(in,
                ui.getOutputDirPanel().getOutputDir(),
                bytes -> reportProgress(bytes, in.length()));
        handleResult(in, out, false);
    }

    /** Forward byte-write progress to the status bar (called from background thread). */
    private void reportProgress(long bytesWritten, long totalBytes) {
        if (totalBytes <= 0) return;
        int pct = (int) Math.min(99, bytesWritten * 100 / totalBytes);
        SwingUtilities.invokeLater(() -> ui.getProgressBar().setProgress(pct));
    }

    /** Post-operation UI update — always called on the background thread, posts to EDT. */
    private void handleResult(File in, File out, boolean wasCompression) {
        if (out != null) {
            fileHandler.setOutputFile(out);
            recentMgr.add(out);
            CompressionStats s = new CompressionStats(in.length(), out.length(), wasCompression);

            SwingUtilities.invokeLater(() -> {
                ui.getStatsPanel().update(s);
                ui.refreshSidebar();
                double pct = s.getRatioPercent();
                String msg = wasCompression
                        ? String.format("Compression complete — %.1f%% saved · %s",
                                pct, out.getName())
                        : "Decompression complete — " + out.getName()
                                + "  (" + fmt(out.length()) + ")";
                ui.getProgressBar().setStatus(ProgressStatusBar.State.SUCCESS, msg);
            });
        } else {
            SwingUtilities.invokeLater(() ->
                    ui.getProgressBar().setStatus(ProgressStatusBar.State.ERROR,
                            "Operation failed — see dialog for details"));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setButtonsEnabled(boolean on) {
        SwingUtilities.invokeLater(() -> {
            huffmanCompressBtn  .setEnabled(on);
            huffmanDecompressBtn.setEnabled(on);
            lzwCompressBtn      .setEnabled(on);
            lzwDecompressBtn    .setEnabled(on);
        });
    }

    private static String fmt(long b) {
        if (b < 1024)           return b + " B";
        if (b < 1_048_576)      return String.format("%.1f KB", b / 1024.0);
        if (b < 1_073_741_824L) return String.format("%.2f MB", b / 1_048_576.0);
        return String.format("%.2f GB", b / 1_073_741_824.0);
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Main app = new Main();
        JPanel pane = app.createContentPane();
        MainWindow.init(pane, e -> app.handleFileOpen());
    }
}
