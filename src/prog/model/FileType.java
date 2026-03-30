package prog.model;

/**
 * All file categories the compressor recognises.
 * Used for smart algorithm selection and badge labelling.
 */
public enum FileType {
    // ── Text / code ───────────────────────────────────────────────────────────
    TEXT,      // .txt .log .md .csv .tsv
    CODE,      // .java .py .js .ts .html .xml .json .yaml .cpp .c .h

    // ── Archive ───────────────────────────────────────────────────────────────
    ARCHIVE,   // .zip .tar .gz .7z .rar .bz2

    // ── Image ─────────────────────────────────────────────────────────────────
    IMAGE_LOSSLESS,  // .png .bmp .tiff .gif
    IMAGE_LOSSY,     // .jpg .jpeg .webp — already compressed, minimal gain

    // ── Video ─────────────────────────────────────────────────────────────────
    VIDEO,     // .mp4 .mov .avi .mkv .wmv .flv — already compressed

    // ── Audio ─────────────────────────────────────────────────────────────────
    AUDIO_LOSSY,    // .mp3 .aac .ogg .m4a — already compressed
    AUDIO_LOSSLESS, // .wav .flac .aiff

    // ── Documents ─────────────────────────────────────────────────────────────
    DOCUMENT,  // .pdf .doc .docx .xls .xlsx .ppt .pptx

    // ── Already compressed (Huffman output) ───────────────────────────────────
    HUFFMAN,

    // ── Already compressed (LZW output) ──────────────────────────────────────
    LZW,

    // ── Unknown / binary ──────────────────────────────────────────────────────
    BINARY;

    /** Returns true when the format is already heavily compressed. */
    public boolean isAlreadyCompressed() {
        return this == IMAGE_LOSSY
            || this == VIDEO
            || this == AUDIO_LOSSY
            || this == ARCHIVE
            || this == HUFFMAN
            || this == LZW;
    }

    /**
     * Whether a small gain is still possible for "already compressed" types
     * via container/metadata stripping tricks. Currently always false —
     * we warn the user instead of silently running compression.
     */
    public boolean benefitsFromRecompression() { return false; }

    /**
     * Detect file type from extension (lower-cased).
     */
    public static FileType detect(String filename) {
        if (filename == null) return BINARY;
        String lower = filename.toLowerCase();

        if (lower.endsWith(".huffz"))                                return HUFFMAN;
        if (lower.endsWith(".lmzwp"))                                return LZW;

        if (lower.endsWith(".txt")  || lower.endsWith(".log")  ||
            lower.endsWith(".md")   || lower.endsWith(".csv")  ||
            lower.endsWith(".tsv"))                                  return TEXT;

        if (lower.endsWith(".java") || lower.endsWith(".py")   ||
            lower.endsWith(".js")   || lower.endsWith(".ts")   ||
            lower.endsWith(".html") || lower.endsWith(".htm")  ||
            lower.endsWith(".xml")  || lower.endsWith(".json") ||
            lower.endsWith(".yaml") || lower.endsWith(".yml")  ||
            lower.endsWith(".cpp")  || lower.endsWith(".c")    ||
            lower.endsWith(".h")    || lower.endsWith(".css")  ||
            lower.endsWith(".sql")  || lower.endsWith(".sh")   ||
            lower.endsWith(".bat")  || lower.endsWith(".toml") ||
            lower.endsWith(".ini")  || lower.endsWith(".cfg"))      return CODE;

        if (lower.endsWith(".png")  || lower.endsWith(".bmp")  ||
            lower.endsWith(".tiff") || lower.endsWith(".tif")  ||
            lower.endsWith(".gif"))                                  return IMAGE_LOSSLESS;

        if (lower.endsWith(".jpg")  || lower.endsWith(".jpeg") ||
            lower.endsWith(".webp"))                                 return IMAGE_LOSSY;

        if (lower.endsWith(".mp4")  || lower.endsWith(".mov")  ||
            lower.endsWith(".avi")  || lower.endsWith(".mkv")  ||
            lower.endsWith(".wmv")  || lower.endsWith(".flv")  ||
            lower.endsWith(".m4v")  || lower.endsWith(".3gp"))      return VIDEO;

        if (lower.endsWith(".mp3")  || lower.endsWith(".aac")  ||
            lower.endsWith(".ogg")  || lower.endsWith(".m4a")  ||
            lower.endsWith(".wma"))                                  return AUDIO_LOSSY;

        if (lower.endsWith(".wav")  || lower.endsWith(".flac") ||
            lower.endsWith(".aiff") || lower.endsWith(".aif"))      return AUDIO_LOSSLESS;

        if (lower.endsWith(".pdf")  || lower.endsWith(".doc")  ||
            lower.endsWith(".docx") || lower.endsWith(".xls")  ||
            lower.endsWith(".xlsx") || lower.endsWith(".ppt")  ||
            lower.endsWith(".pptx"))                                 return DOCUMENT;

        if (lower.endsWith(".zip")  || lower.endsWith(".tar")  ||
            lower.endsWith(".gz")   || lower.endsWith(".7z")   ||
            lower.endsWith(".rar")  || lower.endsWith(".bz2")  ||
            lower.endsWith(".xz"))                                   return ARCHIVE;

        return BINARY;
    }

    /** Short badge string shown in the sidebar. */
    public String badgeLabel(String filename) {
        switch (this) {
            case HUFFMAN: return "HUF";
            case LZW:     return "LZW";
            case VIDEO:   return "VID";
            case IMAGE_LOSSY: case IMAGE_LOSSLESS: return "IMG";
            case AUDIO_LOSSY: case AUDIO_LOSSLESS: return "AUD";
            case ARCHIVE: return "ZIP";
            case DOCUMENT: return "DOC";
            default:
                if (filename != null) {
                    int dot = filename.lastIndexOf('.');
                    if (dot >= 0 && dot < filename.length() - 1)
                        return filename.substring(dot + 1).toUpperCase();
                }
                return "FILE";
        }
    }
}
