package de.firemage.codelinter.agent;

import de.firemage.codelinter.event.Event;
import de.firemage.codelinter.event.GetFieldEvent;
import de.firemage.codelinter.event.MethodEnterEvent;
import de.firemage.codelinter.event.MethodExitThrowEvent;
import de.firemage.codelinter.event.PrimitiveReturnEvent;
import de.firemage.codelinter.event.PutFieldEvent;
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
            System.out.println("AGENT: Writing " + events.size() + " events to " + outPath.toAbsolutePath());
        }));
    }

    public static void setOutPath(Path outPath) {
        EventRecorder.outPath = outPath;
    }

    public static void recordReferenceReturn(String clazz, String methodName, String methodDesc, Object returnedValue) {
        if (returnedValue == null) {
            events.add(new ReferenceReturnEvent(clazz, methodName, methodDesc, null));
        } else {
            events.add(new ReferenceReturnEvent(clazz, methodName, methodDesc,
                returnedValue.getClass().getName().replace(".", "/")));
        }
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      Object value) {
        String targetType = target != null ? target.getClass().getName().replace(".", "/") : "null";
        String valueType = value != null ? value.getClass().getName().replace(".", "/") : "null";
        events.add(new PutFieldEvent(clazz, methodName, methodDesc, targetType, field, valueType));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      boolean value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Boolean.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      char value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Character.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      byte value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Byte.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      short value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Short.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      int value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Integer.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      long value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Long.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      float value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Float.valueOf(value));
    }

    public static void recordPutField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      double value) {
        EventRecorder.recordPutField(clazz, methodName, methodDesc, target, field, Double.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      Object value) {
        String targetType = target != null ? target.getClass().getName().replace(".", "/") : "null";
        String valueType = value != null ? value.getClass().getName().replace(".", "/") : "null";
        events.add(new GetFieldEvent(clazz, methodName, methodDesc, targetType, field, valueType));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      boolean value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Boolean.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      char value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Character.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      byte value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Byte.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      short value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Short.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      int value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Integer.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      long value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Long.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      float value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Float.valueOf(value));
    }

    public static void recordGetField(String clazz, String methodName, String methodDesc, Object target, String field,
                                      double value) {
        EventRecorder.recordGetField(clazz, methodName, methodDesc, target, field, Double.valueOf(value));
    }

    public static void recordExitThrow(String clazz, String methodName, String methodDesc, Throwable throwable) {
        String exceptionType = throwable.getClass().getName().replace(".", "/");
        events.add(new MethodExitThrowEvent(clazz, methodName, methodDesc, exceptionType));
    }

    public static void recordPrimitiveReturn(String clazz, String methodName, String methodDesc, Object value) {
        events.add(new PrimitiveReturnEvent(clazz, methodName, methodDesc, value));
    }

    public static void recordMethodEnter(String clazz, String methodName, String methodDesc) {
        events.add(new MethodEnterEvent(clazz, methodName, methodDesc));
    }
}
