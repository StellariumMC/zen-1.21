package meowing.zen;

import meowing.zen.config.zencfg;
import meowing.zen.utils.EventBus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.nio.file.*;
import java.lang.reflect.Field;
import java.io.IOException;

public class featManager {
    private static final Logger LOGGER = Logger.getLogger("Zen");
    private static final Map<String, FeatureHandler> features = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> configState = new ConcurrentHashMap<>();
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private static final String featpath = "/meowing/zen/feats";
    public static volatile int moduleCount = 0;
    
    static {
        cacheConf();
    }
    
    private static void cacheConf() {
        for (var field : zencfg.class.getDeclaredFields()) {
            if (field.getType() == boolean.class) {
                field.setAccessible(true);
                fieldCache.put(field.getName(), field);
            }
        }
    }
    
    private static class FeatureHandler {
        final Object owner;
        final Runnable registerHandler;
        private final AtomicBoolean isActive = new AtomicBoolean(false);
        
        FeatureHandler(Object owner, Runnable registerHandler) {
            this.owner = owner;
            this.registerHandler = registerHandler;
        }
        
        void toggle(boolean active) {
            if (active && isActive.compareAndSet(false, true)) {
                try {
                    registerHandler.run();
                } catch (Exception e) {
                    LOGGER.warning("Failed to activate feature: " + owner.getClass().getSimpleName() + " - " + e.getMessage());
                    isActive.set(false);
                }
            } else if (!active && isActive.compareAndSet(true, false)) {
                try {
                    EventBus.unregister(owner);
                } catch (Exception e) {
                    LOGGER.warning("Failed to deactivate feature: " + owner.getClass().getSimpleName() + " - " + e.getMessage());
                }
            }
        }
    }
    
    private static void autoDiscFeats() {
        var resource = featManager.class.getResource(featpath);
        if (resource == null) {
            LOGGER.warning("Features directory not found: " + featpath);
            return;
        }
        
        try {
            var uri = resource.toURI();
            var fsPath = uri.getScheme().equals("jar") ? getJarPath(uri) : Paths.get(uri);
            
            discoverFeats(fsPath);
        } catch (Exception e) {
            LOGGER.severe("Failed to discover features: " + e.getMessage());
        }
    }
    
    private static void discoverFeats(Path path) {
        try (var stream = Files.walk(path, 3)) {
            stream.filter(p -> p.toString().endsWith(".class") && p.toString().contains("meowing/zen/feats"))
                .parallel()
                .forEach(featManager::loadFeats);
        } catch (IOException e) {
            LOGGER.severe("Failed to walk features directory: " + e.getMessage());
        }
    }
    
    private static void loadFeats(Path classPath) {
        try {
            var pathStr = classPath.toString();
            int startIdx = pathStr.indexOf("meowing/zen/feats");
            if (startIdx == -1) return;
            
            var className = pathStr.substring(startIdx)
                .replace('/', '.')
                .replace('\\', '.')
                .replace(".class", "");
            
            var clazz = Class.forName(className);
            var initMethod = clazz.getDeclaredMethod("initialize");
            initMethod.invoke(null);
            
            synchronized (featManager.class) {
                moduleCount++;
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.fine("Skipping non-feature class or initialization failed: " + classPath);
        } catch (Exception e) {
            LOGGER.warning("Unexpected error loading feature class " + classPath + ": " + e.getMessage());
        }
    }
    
    private static Path getJarPath(java.net.URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri).getPath(featpath);
        } catch (Exception e) {
            try {
                return FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(featpath);
            } catch (Exception ex) {
                throw new IOException("Failed to access JAR filesystem", ex);
            }
        }
    }
    
    public static void register(Object owner, Runnable registerHandler) {
        Objects.requireNonNull(owner, "Feature owner cannot be null");
        Objects.requireNonNull(registerHandler, "Register handler cannot be null");
        
        var name = owner.getClass().getSimpleName().toLowerCase();
        var handler = new FeatureHandler(owner, registerHandler);
        
        features.put(name, handler);
        var shouldBeActive = getConfigValue(name);
        configState.put(name, shouldBeActive);
        handler.toggle(shouldBeActive);
    }
    
    public static void onConfigChange() {
        features.entrySet().parallelStream().forEach(entry -> {
            var name = entry.getKey();
            var handler = entry.getValue();
            var shouldBeActive = getConfigValue(name);
            var lastValue = configState.get(name);
            
            if (lastValue == null || lastValue != shouldBeActive) {
                handler.toggle(shouldBeActive);
                configState.put(name, shouldBeActive);
            }
        });
    }
    
    private static boolean getConfigValue(String fieldName) {
        var field = fieldCache.get(fieldName);
        if (field == null) return false;
        
        try {
            return (Boolean) field.get(Zen.getConfig());
        } catch (IllegalAccessException e) {
            LOGGER.warning("Failed to access config field: " + fieldName);
            return false;
        } catch (ClassCastException e) {
            LOGGER.warning("Config field is not boolean: " + fieldName);
            return false;
        }
    }
    
    public static void initAll() {
        autoDiscFeats();
    }
    
    public static int getFeatCount() {
        return (int) features.values().stream()
            .mapToInt(handler -> handler.isActive.get() ? 1 : 0)
            .sum();
    }
    
    public static Set<String> getRegFeats() {
        return Collections.unmodifiableSet(features.keySet());
    }
}