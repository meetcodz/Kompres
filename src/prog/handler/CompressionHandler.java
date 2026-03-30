package prog.handler;

import prog.model.FileType;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.function.LongConsumer;

/**
 * Compression handler with:
 *   • True Huffman coding  (custom bit-level encoder, works on ANY file)
 *   • True LZW             (custom dictionary encoder, works on ANY file)
 *   • Smart pre-check: warns when the file is already compressed (JPEG, MP4…)
 *     but still allows the user to proceed.
 *
 * File format — Huffman (.huffz):
 *   [4 bytes] magic = 0x48 0x55 0x46 0x5A ("HUFZ")
 *   [4 bytes] number of distinct symbols (int, big-endian)
 *   for each symbol:
 *     [1 byte]  symbol value
 *     [8 bytes] frequency (long, big-endian)
 *   [8 bytes] total original byte count (long, big-endian)
 *   [remaining] bit-packed encoded data (last byte may be partially filled)
 *
 * File format — LZW (.LmZWp):
 *   [4 bytes] magic = 0x4C 0x5A 0x57 0x50 ("LZWP")
 *   [4 bytes] initial dict size (always 256)
 *   [4 bytes] code width in bits (12)
 *   [8 bytes] original byte count
 *   [remaining] 12-bit codes packed into bytes, big-endian within each pair
 */
public class CompressionHandler {

    public static final String HUFFMAN_EXT = ".huffz";
    public static final String LZW_EXT     = ".LmZWp";

    /** Buffer for stream I/O. */
    private static final int BUFFER = 65_536;

    // ═══════════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════════

    public File compressWithHuffman(File in, File outDir) {
        return compressWithHuffman(in, outDir, null);
    }

    /**
     * @param progress  optional callback receiving bytes written so far (for progress bar)
     */
    public File compressWithHuffman(File in, File outDir, LongConsumer progress) {
        if (!preCheck(in)) return null;
        try {
            File out = resolveOut(in, outDir, HUFFMAN_EXT);
            huffmanEncode(in, out, progress);
            showSuccess("Compression complete!\nOutput: " + out.getName()
                    + "\nSize:   " + fmt(out.length())
                    + "  (was " + fmt(in.length()) + ")");
            return out;
        } catch (Exception e) {
            showError("Huffman compression failed:\n" + e.getMessage());
            return null;
        }
    }

    public File compressWithLZW(File in, File outDir) {
        return compressWithLZW(in, outDir, null);
    }

    public File compressWithLZW(File in, File outDir, LongConsumer progress) {
        if (!preCheck(in)) return null;
        try {
            File out = resolveOut(in, outDir, LZW_EXT);
            lzwEncode(in, out, progress);
            showSuccess("Compression complete!\nOutput: " + out.getName()
                    + "\nSize:   " + fmt(out.length())
                    + "  (was " + fmt(in.length()) + ")");
            return out;
        } catch (Exception e) {
            showError("LZW compression failed:\n" + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Huffman — encode
    // ═══════════════════════════════════════════════════════════════════════

    private static void huffmanEncode(File in, File out, LongConsumer progress)
            throws IOException {

        // 1. Count frequencies (first pass)
        long[] freq = new long[256];
        try (InputStream is = new BufferedInputStream(new FileInputStream(in), BUFFER)) {
            byte[] buf = new byte[BUFFER];
            int r;
            while ((r = is.read(buf)) != -1)
                for (int i = 0; i < r; i++) freq[buf[i] & 0xFF]++;
        }

        // 2. Build Huffman tree
        HuffNode root = buildHuffTree(freq);

        // 3. Generate code table
        String[] codes = new String[256];
        buildCodes(root, "", codes);

        // 4. Write output
        long originalLen = in.length();
        try (InputStream is = new BufferedInputStream(new FileInputStream(in), BUFFER);
             DataOutputStream dos = new DataOutputStream(
                     new BufferedOutputStream(new FileOutputStream(out), BUFFER))) {

            // --- header ---
            dos.write(new byte[]{'H','U','F','Z'});

            // symbol table
            int distinct = 0;
            for (long f : freq) if (f > 0) distinct++;
            dos.writeInt(distinct);
            for (int i = 0; i < 256; i++) {
                if (freq[i] > 0) {
                    dos.writeByte(i);
                    dos.writeLong(freq[i]);
                }
            }
            dos.writeLong(originalLen);

            // --- bit-packed data ---
            byte[] buf = new byte[BUFFER];
            int r;
            int bitBuf = 0, bitCount = 0;
            long written = 0;
            while ((r = is.read(buf)) != -1) {
                for (int i = 0; i < r; i++) {
                    String code = codes[buf[i] & 0xFF];
                    for (char c : code.toCharArray()) {
                        bitBuf = (bitBuf << 1) | (c == '1' ? 1 : 0);
                        bitCount++;
                        if (bitCount == 8) {
                            dos.writeByte(bitBuf);
                            written++;
                            bitBuf = 0; bitCount = 0;
                            if (progress != null) progress.accept(written);
                        }
                    }
                }
            }
            // flush remaining bits
            if (bitCount > 0) {
                dos.writeByte(bitBuf << (8 - bitCount));
            }
        }
    }

    // ── Huffman tree ──────────────────────────────────────────────────────────

    private static HuffNode buildHuffTree(long[] freq) {
        PriorityQueue<HuffNode> pq = new PriorityQueue<>(
                Comparator.comparingLong(n -> n.freq));
        for (int i = 0; i < 256; i++)
            if (freq[i] > 0) pq.add(new HuffNode((byte) i, freq[i], null, null));
        if (pq.isEmpty()) pq.add(new HuffNode((byte) 0, 1, null, null)); // edge case
        while (pq.size() > 1) {
            HuffNode a = pq.poll(), b = pq.poll();
            pq.add(new HuffNode((byte) 0, a.freq + b.freq, a, b));
        }
        return pq.poll();
    }

    private static void buildCodes(HuffNode node, String prefix, String[] codes) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            codes[node.symbol & 0xFF] = prefix.isEmpty() ? "0" : prefix;
            return;
        }
        buildCodes(node.left,  prefix + "0", codes);
        buildCodes(node.right, prefix + "1", codes);
    }

    private static final class HuffNode {
        final byte    symbol;
        final long    freq;
        final HuffNode left, right;
        HuffNode(byte s, long f, HuffNode l, HuffNode r) {
            symbol = s; freq = f; left = l; right = r;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LZW — encode  (12-bit codes, max dict = 4096)
    // ═══════════════════════════════════════════════════════════════════════

    private static final int LZW_BITS    = 12;
    private static final int LZW_MAXCODE = (1 << LZW_BITS);   // 4096

    private static void lzwEncode(File in, File out, LongConsumer progress)
            throws IOException {

        long originalLen = in.length();

        // Build initial dictionary: single-byte strings 0..255
        Map<String, Integer> dict = new HashMap<>(512);
        for (int i = 0; i < 256; i++) dict.put(String.valueOf((char) i), i);
        int nextCode = 256;

        try (InputStream is = new BufferedInputStream(new FileInputStream(in), BUFFER);
             DataOutputStream dos = new DataOutputStream(
                     new BufferedOutputStream(new FileOutputStream(out), BUFFER))) {

            // Header
            dos.write(new byte[]{'L','Z','W','P'});
            dos.writeInt(256);
            dos.writeInt(LZW_BITS);
            dos.writeLong(originalLen);

            // Encode
            List<Integer> codes = new ArrayList<>();
            StringBuilder w = new StringBuilder();
            int b;
            long bytesRead = 0;
            while ((b = is.read()) != -1) {
                bytesRead++;
                char c = (char) b;
                String wc = w.toString() + c;
                if (dict.containsKey(wc)) {
                    w = new StringBuilder(wc);
                } else {
                    codes.add(dict.get(w.toString()));
                    if (nextCode < LZW_MAXCODE) {
                        dict.put(wc, nextCode++);
                    }
                    w = new StringBuilder(String.valueOf(c));
                }
                if (progress != null) progress.accept(bytesRead);
            }
            if (w.length() > 0 && dict.containsKey(w.toString())) {
                codes.add(dict.get(w.toString()));
            }

            // Pack 12-bit codes into bytes (big-endian pairs → 3 bytes per 2 codes)
            int i = 0;
            while (i < codes.size()) {
                int c1 = codes.get(i++);
                if (i < codes.size()) {
                    int c2 = codes.get(i++);
                    dos.writeByte((c1 >> 4) & 0xFF);
                    dos.writeByte(((c1 & 0xF) << 4) | ((c2 >> 8) & 0xF));
                    dos.writeByte(c2 & 0xFF);
                } else {
                    // odd code at end
                    dos.writeByte((c1 >> 4) & 0xFF);
                    dos.writeByte((c1 & 0xF) << 4);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Warn if the file format is already compressed (JPEG, MP4, …).
     * Returns true if the operation should continue.
     */
    private static boolean preCheck(File f) {
        FileType ft = FileType.detect(f.getName());
        if (ft.isAlreadyCompressed()) {
            int choice = JOptionPane.showConfirmDialog(null,
                    "⚠  The file \"" + f.getName() + "\" is a " + ft.name().replace('_', ' ')
                    + " and is already compressed internally.\n\n"
                    + "Compressing it again will likely produce a LARGER output,\n"
                    + "not a smaller one, and may take a long time.\n\n"
                    + "Proceed anyway?",
                    "Already-Compressed File Detected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            return choice == JOptionPane.YES_OPTION;
        }
        return true;
    }

    static File resolveOut(File in, File outDir, String ext) {
        if (outDir == null || outDir.equals(in.getParentFile()))
            return new File(in.getPath() + ext);
        return new File(outDir, in.getName() + ext);
    }

    public double calculateCompressionRatio(long orig, long compressed) {
        return (1.0 - (double) compressed / orig) * 100.0;
    }

    private static String fmt(long bytes) {
        if (bytes < 1024)         return bytes + " B";
        if (bytes < 1048576)      return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1073741824L)  return String.format("%.2f MB", bytes / 1048576.0);
        return String.format("%.2f GB", bytes / 1073741824.0);
    }

    private static void showSuccess(String msg) {
        JOptionPane.showMessageDialog(null, msg,
                "Compression Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg,
                "Compression Error", JOptionPane.ERROR_MESSAGE);
    }
}
