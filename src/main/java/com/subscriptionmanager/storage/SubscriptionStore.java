package com.subscriptionmanager.storage;

import com.subscriptionmanager.model.BillingCycle;
import com.subscriptionmanager.model.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads subscriptions as a CSV file so they persist between runs.
 * Format: {@code id,name,price,cycle,category} with a header row. Files saved by
 * earlier versions, which have no category column, still load (with no category).
 */
public class SubscriptionStore {

    private static final String HEADER = "id,name,price,cycle,category";
    private static final String LEGACY_HEADER = "id,name,price,cycle";

    private final Path file;

    public SubscriptionStore(Path file) {
        this.file = file;
    }

    /** The default location: {@code ~/.subscription-manager/subscriptions.csv}. */
    public static SubscriptionStore defaultStore() {
        Path dir = Path.of(System.getProperty("user.home"), ".subscription-manager");
        return new SubscriptionStore(dir.resolve("subscriptions.csv"));
    }

    public Path getFile() {
        return file;
    }

    public List<Subscription> load() throws IOException {
        List<Subscription> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || (i == 0 && (line.equals(HEADER) || line.equals(LEGACY_HEADER)))) {
                continue;
            }
            List<String> fields = parseLine(line);
            if (fields.size() != 4 && fields.size() != 5) {
                throw new IOException("Malformed line " + (i + 1) + " in " + file + ": " + line);
            }
            try {
                result.add(new Subscription(
                        fields.get(0),
                        fields.get(1),
                        new BigDecimal(fields.get(2)),
                        BillingCycle.valueOf(fields.get(3)),
                        fields.size() == 5 ? fields.get(4) : ""));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid data on line " + (i + 1) + " in " + file + ": " + e.getMessage(), e);
            }
        }
        return result;
    }

    public void save(List<Subscription> subscriptions) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Subscription s : subscriptions) {
            lines.add(String.join(",",
                    escape(s.id()),
                    escape(s.name()),
                    s.price().toPlainString(),
                    s.cycle().name(),
                    escape(s.category())));
        }
        // Write to a temp file then move, so a crash mid-write can't corrupt existing data.
        Path temp = Files.createTempFile(parent, "subscriptions", ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    static String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
