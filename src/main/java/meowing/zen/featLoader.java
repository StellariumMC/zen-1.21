package meowing.zen;

import meowing.zen.feats.meowing.automeow;
import meowing.zen.feats.meowing.meowsounds;
import meowing.zen.feats.meowing.meowdeathsounds;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class featLoader {
    private static final List<Class<?>> feats = Arrays.asList(
            automeow.class,
            meowsounds.class,
            meowdeathsounds.class
    );
    public static int moduleCount = 0;
    public static void initializeAll() {
        for (Class<?> moduleClass : feats) {
            try {
                Method initMethod = moduleClass.getDeclaredMethod("initialize");
                initMethod.invoke(null);
                moduleCount++;
            } catch (Exception e) {
                System.err.println("[Zen] Failed to initialize " + moduleClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
