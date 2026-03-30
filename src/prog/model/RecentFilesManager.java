package prog.model;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

/** Maintains a bounded, persisted list of recently accessed files. */
public class RecentFilesManager {

    private static final int    MAX      = 10;
    private static final String NODE     = "prog/kompres";
    private static final String PREFIX   = "recent.";

    private final Deque<RecentFile> entries = new ArrayDeque<>();
    private final Preferences       prefs   = Preferences.userRoot().node(NODE);

    public RecentFilesManager() { load(); }

    public void add(File file) {
        entries.removeIf(r -> r.getAbsolutePath().equals(file.getAbsolutePath()));
        entries.addFirst(new RecentFile(file));
        while (entries.size() > MAX) entries.removeLast();
        save();
    }

    public List<RecentFile> getEntries() { return new ArrayList<>(entries); }

    private void save() {
        for (int i = 0; i < MAX; i++) prefs.remove(PREFIX + i);
        int i = 0;
        for (RecentFile rf : entries) prefs.put(PREFIX + i++, rf.getAbsolutePath());
    }

    private void load() {
        for (int i = MAX - 1; i >= 0; i--) {
            String path = prefs.get(PREFIX + i, null);
            if (path != null) {
                File f = new File(path);
                if (f.exists()) entries.addFirst(new RecentFile(f));
            }
        }
    }
}
