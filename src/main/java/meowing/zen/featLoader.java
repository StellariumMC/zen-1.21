package meowing.zen;

import java.nio.file.*;
import java.util.Collections;

public class featLoader {
    public static int moduleCount = 0;

    public static void initAll() {
        try {
            var resource = featLoader.class.getResource("/meowing/zen/feats");
            if (resource == null) return;

            var uri = resource.toURI();
            var path = uri.getScheme().equals("jar")
                    ? FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath("/meowing/zen/feats")
                    : Paths.get(uri);

            try (var stream = Files.walk(path, 2)) {
                stream.filter(p -> {
                            var pathStr = p.toString();
                            return pathStr.endsWith(".class") && pathStr.contains("meowing/zen/feats");
                        })
                        .forEach(p -> {
                            try {
                                var pathStr = p.toString();
                                var className = pathStr.substring(pathStr.indexOf("meowing/zen/feats"))
                                        .replace('/', '.').replace('\\', '.')
                                        .replace(".class", "");
                                Class.forName(className).getDeclaredMethod("initialize").invoke(null);
                                moduleCount++;
                            } catch (Exception ignored) {}
                        });
            }
        } catch (Exception ignored) {}
    }
}