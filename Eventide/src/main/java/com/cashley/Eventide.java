package com.cashley;

import com.cashley.annotations.Event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.*;

public class Eventide {

    private static final Map<String, List<ListenerMethod>> listeners = new ConcurrentHashMap<>();
    private static final Map<String, List<ListenerMethod>> wildcardListeners = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void register(Object obj) {
        Class<?> clazz = (obj instanceof Class) ? (Class<?>) obj : obj.getClass();

        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Event.class)) continue;

            Event event = m.getAnnotation(Event.class);
            String name = event.value();

            m.setAccessible(true);

            Object instance = Modifier.isStatic(m.getModifiers()) ? null : obj;

            if (name.contains("*") || name.contains("?")) {
                wildcardListeners
                        .computeIfAbsent(name, k -> new CopyOnWriteArrayList<>())
                        .add(new ListenerMethod(instance, m, event.async()));
            } else {
                listeners
                        .computeIfAbsent(name, k -> new CopyOnWriteArrayList<>())
                        .add(new ListenerMethod(instance, m, event.async()));
            }
        }
    }

    public static void emit(String event) {
        if (executor.isTerminated()) {
            System.out.println("Executor was terminated");
            return;
        }

        List<ListenerMethod> list = listeners.get(event);
        if (list != null) dispatch(list);

        for (Map.Entry<String, List<ListenerMethod>> entry : wildcardListeners.entrySet()) {
            if (matches(entry.getKey(), event)) {
                dispatch(entry.getValue());
            }
        }
    }

    public static void shutdown() {
        executor.shutdown();
    }

    private static boolean matches(String pattern, String event) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");

        return event.matches(regex);
    }

    private static void dispatch(List<ListenerMethod> list) {
        for (ListenerMethod lm : list) {
            Runnable task = () -> {
                try {
                    Method m = lm.method;

                    if (Modifier.isStatic(m.getModifiers())) {
                        m.invoke(null);
                    } else {
                        m.invoke(lm.instance);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            if (lm.async) {
                executor.submit(task);
            } else {
                task.run();
            }
        }
    }
}