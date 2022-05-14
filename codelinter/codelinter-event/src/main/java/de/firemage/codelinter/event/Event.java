package de.firemage.codelinter.event;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public interface Event {
    static void write(List<Event> events, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Event event : events) {
                writer.write(event.format());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<Event> read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return reader.lines().map(line -> {
                String[] parts = line.split(":");
                return switch (parts[0]) {
                    case "RefRet" -> new ReferenceReturnEvent(parts[1], parts[2], parts[3], parts[4]);
                    default -> throw new IllegalStateException("Unknown event type '" + parts[0] + "'");
                };
            }).collect(Collectors.toList());
        }
    }

    String format();
}
