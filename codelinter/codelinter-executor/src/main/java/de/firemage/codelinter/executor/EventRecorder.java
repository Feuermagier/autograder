package de.firemage.codelinter.executor;

import de.firemage.codelinter.event.Event;
import de.firemage.codelinter.event.ReferenceReturnEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EventRecorder {
    private static final List<Event> events = new ArrayList<>();
    private static Path outPath;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Event.write(events, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void setOutPath(Path outPath) {
        EventRecorder.outPath = outPath;
    }

    public static void recordReferenceReturn(String clazz, String methodName, String methodDesc, Object returnedValue) {
        if (returnedValue == null) {
            events.add(new ReferenceReturnEvent(clazz, methodName, methodDesc, null));
        } else {
            events.add(new ReferenceReturnEvent(clazz, methodName, methodDesc, returnedValue.getClass().getName().replace(".", "/")));
        }
    }
}
