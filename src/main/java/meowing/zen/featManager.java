package meowing.zen;

import meowing.zen.config.zencfg;
import meowing.zen.utils.EventBus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final Set<String> immutableFeats = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger moduleCount = new AtomicInteger(0);
    
    static {
        cacheConf();
    }
    
    private static void cacheConf() {
        Field[] fields = zencfg.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == boolean.class) {
                field.setAccessible(true);
                fieldCache.put(field.getName(), field);
            }
        }
    }
    
    private static class FeatureHandler {
        final Object owner;
        final Runnable registerHandler;
        volatile boolean isActive = false;
        
        FeatureHandler(Object owner, Runnable registerHandler) {
            this.owner = owner;
            this.registerHandler = registerHandler;
        }
        
        boolean toggle(boolean active) {
            if (active && !isActive) {
                try {
                    registerHandler.run();
                    isActive = true;
                    return true;
                } catch (Exception e) {
                    LOGGER.warning("Failed to activate feature: " + owner.getClass().getSimpleName() + " - " + e.getMessage());
                    return false;
                }
            } else if (!active && isActive) {
                try {
                    EventBus.unregister(owner);
                    isActive = false;
                    return true;
                } catch (Exception e) {
                    LOGGER.warning("Failed to deactivate feature: " + owner.getClass().getSimpleName() + " - " + e.getMessage());
                    return false;
                }
            }
            return false;
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
            if (uri.getScheme().equals("jar")) {
                try (var fs = getOrCreateFileSystem(uri)) {
                    discoverFeats(fs.getPath(featpath));
                }
            } else {
                discoverFeats(Paths.get(uri));
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to discover features: " + e.getMessage());
        }
    }
    
    private static FileSystem getOrCreateFileSystem(java.net.URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        }
    }
    
    private static void discoverFeats(Path path) {
        try (var stream = Files.walk(path, 3)) {
            stream.filter(p -> {
                String pathStr = p.toString();
                return pathStr.endsWith(".class") && pathStr.contains("meowing/zen/feats");
            })
            .parallel()
            .forEach(featManager::loadFeats);
        } catch (IOException e) {
            LOGGER.severe("Failed to walk features directory: " + e.getMessage());
        }
    }
    
    private static void loadFeats(Path classPath) {
        try {
            String pathStr = classPath.toString();
            int startIdx = pathStr.indexOf("meowing/zen/feats");
            if (startIdx == -1) return;
            
            String className = pathStr.substring(startIdx)
                .replace('/', '.')
                .replace('\\', '.')
                .replace(".class", "");
            
            Class<?> clazz = Class.forName(className);
            var initMethod = clazz.getDeclaredMethod("initialize");
            initMethod.invoke(null);
            
            moduleCount.incrementAndGet();
        } catch (ReflectiveOperationException e) {
            LOGGER.fine("Skipping non-feature class or initialization failed: " + classPath);
        } catch (Exception e) {
            LOGGER.warning("Unexpected error loading feature class " + classPath + ": " + e.getMessage());
        }
    }
    
    public static void register(Object owner, Runnable registerHandler) {
        Objects.requireNonNull(owner, "Feature owner cannot be null");
        Objects.requireNonNull(registerHandler, "Register handler cannot be null");
        
        String name = owner.getClass().getSimpleName().toLowerCase();
        var handler = new FeatureHandler(owner, registerHandler);
        
        features.put(name, handler);
        immutableFeats.add(name);
        boolean shouldBeActive = getConfigValue(name);
        configState.put(name, shouldBeActive);
        handler.toggle(shouldBeActive);
    }
    
    public static void onConfigChange() {
        features.entrySet().parallelStream().forEach(entry -> {
            String name = entry.getKey();
            var handler = entry.getValue();
            boolean shouldBeActive = getConfigValue(name);
            Boolean lastValue = configState.get(name);
            
            if (lastValue == null || lastValue != shouldBeActive) 
                if (handler.toggle(shouldBeActive)) configState.put(name, shouldBeActive);
        });
    }
    
    private static boolean getConfigValue(String fieldName) {
        Field field = fieldCache.get(fieldName);
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
            .mapToInt(handler -> handler.isActive ? 1 : 0)
            .sum();
    }
    
    public static Set<String> getRegFeats() {
        return immutableFeats;
    }
    
    public static int getModuleCount() {
        return moduleCount.get();
    }
}