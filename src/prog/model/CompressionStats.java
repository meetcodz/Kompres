package prog.model;

/** Statistics for a single compress/decompress operation. */
public class CompressionStats {
    private final long    originalBytes;
    private final long    outputBytes;
    private final boolean isCompression;

    public CompressionStats(long orig, long out, boolean isCompression) {
        this.originalBytes = orig;
        this.outputBytes   = out;
        this.isCompression = isCompression;
    }

    public long    getOriginalBytes() { return originalBytes; }
    public long    getOutputBytes()   { return outputBytes; }
    public boolean isCompression()    { return isCompression; }

    /** Positive = bytes saved; negative = file grew. */
    public double getRatioPercent() {
        if (originalBytes == 0) return 0;
        return (1.0 - (double) outputBytes / originalBytes) * 100.0;
    }

    public long getDeltaBytes() { return originalBytes - outputBytes; }
}
