package de.firemage.autograder.event;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        return Event.read(Files.newInputStream(path));
    }

    static List<Event> read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().map(line -> {
                String[] parts = line.split(":");
                return switch(parts[0]) {
                    case "RefRet" -> new ReferenceReturnEvent(parts[1], parts[2], parts[3], parts[4]);
                    case "PrimRet" -> new PrimitiveReturnEvent(parts[1], parts[2], parts[3], parts[4]);
                    case "GetField" -> new GetFieldEvent(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    case "PutField" -> new PutFieldEvent(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
                    case "Enter" -> new MethodEnterEvent(parts[1], parts[2], parts[3]);
                    case "Throw" -> new MethodExitThrowEvent(parts[1], parts[2], parts[3], parts[4]);
                    default -> throw new IllegalStateException("Unknown event type '" + parts[0] + "'");
                };
            }).collect(Collectors.toList());
        }
    }

    String format();
}
