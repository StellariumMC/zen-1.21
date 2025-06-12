package meowing.zen;

import meowing.zen.config.zencfg;
import meowing.zen.utils.EventBus;
import java.util.*;
import java.nio.file.*;

public class featManager {
    private static final List<Runnable> featureHandlers = new ArrayList<>();
    private static final Map<String, String> featureConfigMap = new HashMap<>();
    private static zencfg lastConfig;
    
    static {
        for (var field : Zen.getConfig().getClass().getDeclaredFields()) 
            if (field.getType() == boolean.class) featureConfigMap.put(field.getName(), field.getName());
        autoDiscoverFeatures();
    }
    
    private static void autoDiscoverFeatures() {
        try {
            var resource = featManager.class.getResource("/meowing/zen/feats");
            if (resource == null) return;
            var uri = resource.toURI();
            var fsPath = uri.getScheme().equals("jar") ? getJarPath(uri) : Paths.get(uri);
            try (var stream = Files.walk(fsPath, 3)) {
                stream.filter(p -> p.toString().endsWith(".class") && p.toString().contains("meowing/zen/feats"))
                    .forEach(p -> {
                        try {
                            var pathStr = p.toString();
                            int startIdx = pathStr.indexOf("meowing/zen/feats");
                            if (startIdx != -1) {
                                var className = pathStr.substring(startIdx).replace('/', '.').replace('\\', '.').replace(".class", "");
                                var featureClass = Class.forName(className);
                                try {
                                    featureClass.getDeclaredMethod("initialize").invoke(null);
                                } catch (Exception ignored) {}
                            }
                        } catch (Exception ignored) {}
                    });
            }
        } catch (Exception ignored) {}
    }
    
    private static Path getJarPath(java.net.URI uri) {
        try {
            return FileSystems.getFileSystem(uri).getPath("/meowing/zen/feats");
        } catch (Exception e) {
            try {
                return FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath("/meowing/zen/feats");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static void register(Object owner, Runnable handler) {
        featureHandlers.add(handler);
        EventBus.register(owner.getClass(), owner, e -> { if (shouldRun(owner)) handler.run(); });
    }
    
    public static void updateFeatures() {
        zencfg config = Zen.getConfig();
        if (config != lastConfig) {
            lastConfig = config;
            featureHandlers.forEach(Runnable::run);
        }
    }
    
    private static boolean shouldRun(Object feature) {
        String className = feature.getClass().getSimpleName().toLowerCase();
        String configField = featureConfigMap.getOrDefault(className, className);
        return getConfigValue(Zen.getConfig(), configField);
    }
    
    private static boolean getConfigValue(zencfg config, String fieldName) {
        try {
            var field = config.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Boolean) field.get(config);
        } catch (Exception e) {
            return false;
        }
    }
}