package prog.handler;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.function.LongConsumer;

/**
 * Decompression handler — mirrors CompressionHandler exactly.
 *
 * Reads the custom .huffz / .LmZWp file formats written by CompressionHandler
 * and reconstructs the original byte stream faithfully for ANY file type.
 */
public class DecompressionHandler {

    public static final String HUFFMAN_EXT = ".huffz";
    public static final String LZW_EXT     = ".LmZWp";

    private static final int BUFFER = 65_536;

    // ═══════════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════════

    public File decompressHuffman(File in, File outDir) {
        return decompressHuffman(in, outDir, null);
    }

    public File decompressHuffman(File in, File outDir, LongConsumer progress) {
        try {
            File out = resolveOut(in, outDir, HUFFMAN_EXT);
            huffmanDecode(in, out, progress);
            showSuccess("Decompression complete!\nOutput: " + out.getName()
                    + "\nSize:   " + fmt(out.length()));
            return out;
        } catch (Exception e) {
            showError("Huffman decompression failed:\n" + e.getMessage());
            return null;
        }
    }

    public File decompressLZW(File in, File outDir) {
        return decompressLZW(in, outDir, null);
    }

    public File decompressLZW(File in, File outDir, LongConsumer progress) {
        try {
            File out = resolveOut(in, outDir, LZW_EXT);
            lzwDecode(in, out, progress);
            showSuccess("Decompression complete!\nOutput: " + out.getName()
                    + "\nSize:   " + fmt(out.length()));
            return out;
        } catch (Exception e) {
            showError("LZW decompression failed:\n" + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Huffman — decode
    // ═══════════════════════════════════════════════════════════════════════

    private static void huffmanDecode(File in, File out, LongConsumer progress)
            throws IOException {

        try (DataInputStream dis = new DataInputStream(
                     new BufferedInputStream(new FileInputStream(in), BUFFER));
             BufferedOutputStream bos = new BufferedOutputStream(
                     new FileOutputStream(out), BUFFER)) {

            // 1. Verify magic
            byte[] magic = new byte[4];
            dis.readFully(magic);
            if (magic[0] != 'H' || magic[1] != 'U' || magic[2] != 'F' || magic[3] != 'Z')
                throw new IOException("Not a valid .huffz file (bad magic).");

            // 2. Read symbol table
            int distinct = dis.readInt();
            long[] freq = new long[256];
            for (int i = 0; i < distinct; i++) {
                int sym = dis.readByte() & 0xFF;
                freq[sym] = dis.readLong();
            }
            long originalLen = dis.readLong();

            // 3. Rebuild Huffman tree
            HuffNode root = buildHuffTree(freq);

            // 4. Decode bits
            long written = 0;
            HuffNode cur = root;
            byte[] buf = new byte[BUFFER];
            int r;
            outer:
            while ((r = dis.read(buf)) != -1) {
                for (int i = 0; i < r; i++) {
                    int byteVal = buf[i] & 0xFF;
                    for (int bit = 7; bit >= 0; bit--) {
                        int b = (byteVal >> bit) & 1;
                        cur = (b == 0) ? cur.left : cur.right;
                        if (cur == null) throw new IOException("Corrupt data: null tree node.");
                        if (cur.left == null && cur.right == null) {
                            bos.write(cur.symbol & 0xFF);
                            written++;
                            if (progress != null) progress.accept(written);
                            if (written >= originalLen) break outer;
                            cur = root;
                        }
                    }
                }
            }
        }
    }

    // ── Huffman tree (same as encoder) ────────────────────────────────────────

    private static HuffNode buildHuffTree(long[] freq) {
        PriorityQueue<HuffNode> pq = new PriorityQueue<>(
                Comparator.comparingLong(n -> n.freq));
        for (int i = 0; i < 256; i++)
            if (freq[i] > 0) pq.add(new HuffNode((byte) i, freq[i], null, null));
        if (pq.isEmpty()) pq.add(new HuffNode((byte) 0, 1, null, null));
        while (pq.size() > 1) {
            HuffNode a = pq.poll(), b = pq.poll();
            pq.add(new HuffNode((byte) 0, a.freq + b.freq, a, b));
        }
        return pq.poll();
    }

    private static final class HuffNode {
        final byte     symbol;
        final long     freq;
        final HuffNode left, right;
        HuffNode(byte s, long f, HuffNode l, HuffNode r) {
            symbol = s; freq = f; left = l; right = r;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LZW — decode
    // ═══════════════════════════════════════════════════════════════════════

    private static void lzwDecode(File in, File out, LongConsumer progress)
            throws IOException {

        try (DataInputStream dis = new DataInputStream(
                     new BufferedInputStream(new FileInputStream(in), BUFFER));
             BufferedOutputStream bos = new BufferedOutputStream(
                     new FileOutputStream(out), BUFFER)) {

            // 1. Verify magic
            byte[] magic = new byte[4];
            dis.readFully(magic);
            if (magic[0] != 'L' || magic[1] != 'Z' || magic[2] != 'W' || magic[3] != 'P')
                throw new IOException("Not a valid .LmZWp file (bad magic).");

            int initDictSize = dis.readInt();   // 256
            int codeBits     = dis.readInt();   // 12
            long originalLen = dis.readLong();

            int maxCode = (1 << codeBits);      // 4096

            // 2. Read all packed 12-bit codes
            byte[] raw = dis.readAllBytes();
            List<Integer> codes = new ArrayList<>(raw.length);
            int i = 0;
            while (i + 2 < raw.length) {
                int b0 = raw[i]   & 0xFF;
                int b1 = raw[i+1] & 0xFF;
                int b2 = raw[i+2] & 0xFF;
                codes.add((b0 << 4) | (b1 >> 4));
                codes.add(((b1 & 0xF) << 8) | b2);
                i += 3;
            }
            // handle trailing odd byte pair
            if (i + 1 < raw.length) {
                int b0 = raw[i]   & 0xFF;
                int b1 = raw[i+1] & 0xFF;
                codes.add((b0 << 4) | (b1 >> 4));
            }

            // 3. Decode
            Map<Integer, String> dict = new HashMap<>(512);
            for (int c = 0; c < 256; c++) dict.put(c, String.valueOf((char) c));
            int nextCode = 256;

            if (codes.isEmpty()) return;

            long written = 0;
            String w = dict.get(codes.get(0));
            for (char ch : w.toCharArray()) {
                bos.write(ch);
                written++;
            }
            if (progress != null) progress.accept(written);

            for (int idx = 1; idx < codes.size(); idx++) {
                int code = codes.get(idx);
                String entry;
                if (dict.containsKey(code)) {
                    entry = dict.get(code);
                } else if (code == nextCode) {
                    entry = w + w.charAt(0);
                } else {
                    throw new IOException("Corrupt LZW data at code index " + idx);
                }
                for (char ch : entry.toCharArray()) {
                    bos.write(ch);
                    written++;
                    if (written >= originalLen) break;
                }
                if (progress != null) progress.accept(written);
                if (nextCode < maxCode) {
                    dict.put(nextCode++, w + entry.charAt(0));
                }
                w = entry;
                if (written >= originalLen) break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════════

    private static File resolveOut(File compressed, File outDir, String ext) {
        String name = compressed.getName();
        if (name.toLowerCase().endsWith(ext.toLowerCase()))
            name = name.substring(0, name.length() - ext.length());
        File dir = (outDir != null) ? outDir : compressed.getParentFile();
        return new File(dir, name);
    }

    private static String fmt(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1048576)     return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1073741824L) return String.format("%.2f MB", bytes / 1048576.0);
        return String.format("%.2f GB", bytes / 1073741824.0);
    }

    private static void showSuccess(String msg) {
        JOptionPane.showMessageDialog(null, msg,
                "Decompression Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg,
                "Decompression Error", JOptionPane.ERROR_MESSAGE);
    }
}
