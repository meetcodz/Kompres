package prog.model;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Represents one entry in the recent-files sidebar list. */
public class RecentFile {

    private final String        absolutePath;
    private final String        name;
    private final long          sizeBytes;
    private final LocalDateTime accessedAt;
    private final FileType      fileType;

    public RecentFile(File file) {
        this.absolutePath = file.getAbsolutePath();
        this.name         = file.getName();
        this.sizeBytes    = file.length();
        this.accessedAt   = LocalDateTime.now();
        this.fileType     = FileType.detect(file.getName());
    }

    public String   getAbsolutePath() { return absolutePath; }
    public String   getName()         { return name; }
    public long     getSizeBytes()    { return sizeBytes; }
    public FileType getFileType()     { return fileType; }

    public String getRelativeTime() {
        long mins = ChronoUnit.MINUTES.between(accessedAt, LocalDateTime.now());
        if (mins < 1)  return "just now";
        if (mins < 60) return mins + "m ago";
        long hrs = ChronoUnit.HOURS.between(accessedAt, LocalDateTime.now());
        if (hrs < 24)  return hrs + "h ago";
        long days = ChronoUnit.DAYS.between(accessedAt, LocalDateTime.now());
        if (days == 1) return "yesterday";
        return accessedAt.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    public String getBadgeLabel() { return fileType.badgeLabel(name); }
}
