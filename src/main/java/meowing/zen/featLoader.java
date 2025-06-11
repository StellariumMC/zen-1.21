package meowing.zen;

import java.nio.file.*;
import java.util.Collections;

public class featLoader {
    public static int moduleCount = 0;

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

    public static void initAll() {
        try {
            var resource = featLoader.class.getResource("/meowing/zen/feats");
            if (resource == null) return;

            var uri = resource.toURI();
            var fsPath = uri.getScheme().equals("jar")
                ? getJarPath(uri)
                : Paths.get(uri);

            try (var stream = Files.walk(fsPath, 2)) {
                stream.filter(p -> p.toString().endsWith(".class") && p.toString().contains("meowing/zen/feats"))
                      .forEach(p -> {
                          try {
                              var pathStr = p.toString();
                              int startIdx = pathStr.indexOf("meowing/zen/feats");
                              if (startIdx == -1) return;

                              var className = pathStr.substring(startIdx)
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
