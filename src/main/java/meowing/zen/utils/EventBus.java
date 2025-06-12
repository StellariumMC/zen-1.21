package meowing.zen.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Object, Consumer<?>>> listeners = new ConcurrentHashMap<>();
    
    public static <T> void register(Class<T> eventType, Object owner, Consumer<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new ConcurrentHashMap<>()).put(owner, handler);
    }
    
    public static void unregister(Object owner) {
        listeners.values().forEach(map -> map.remove(owner));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> void fire(T event) {
        var handlers = listeners.get(event.getClass());
        if (handlers != null && !handlers.isEmpty()) {
            handlers.values().parallelStream().forEach(handler -> {
                try {
                    ((Consumer<T>) handler).accept(event);
                } catch (Exception ignored) {}
            });
        }
    }
}