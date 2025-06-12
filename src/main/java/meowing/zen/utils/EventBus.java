package meowing.zen.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {
    private static final Map<Class<?>, Map<Object, Consumer<?>>> listeners = new HashMap<>();
    
    public static <T> void register(Class<T> eventType, Object owner, Consumer<T> handler) {
        listeners.computeIfAbsent(eventType, k -> new HashMap<>()).put(owner, handler);
    }
    
    public static void unregister(Object owner) {
        listeners.values().forEach(map -> map.remove(owner));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> void fire(T event) {
        Map<Object, Consumer<?>> handlers = listeners.get(event.getClass());
        if (handlers != null) {
            handlers.values().forEach(handler -> ((Consumer<T>) handler).accept(event));
        }
    }
}