package meowing.zen.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import meowing.zen.featManager;

public class zencfg {
    public static ConfigClassHandler<zencfg> Handler = ConfigClassHandler.createBuilder(zencfg.class)
            .id(net.minecraft.util.Identifier.of("meowing", "zen"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("zen.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry public boolean automeow = false;
    @SerialEntry public boolean meowsounds = false;
    @SerialEntry public boolean meowdeathsounds = false;
    @SerialEntry public boolean cleanmsg = false;
    @SerialEntry public boolean cleanjoin = false;
    @SerialEntry public String vipcolor = "a";
    @SerialEntry public String vippluscolor = "a";
    @SerialEntry public String mvpcolor = "b";
    @SerialEntry public String mvppluscolor = "b";
    @SerialEntry public String mvppluspluscolor = "6";
    @SerialEntry public boolean slayertimer = false;
    @SerialEntry public boolean slayerhighlight = false;

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(Handler, ((defaults, config, builder) -> builder
                .title(Text.literal("Zen Configuration"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Meowing"))
                                .option(createBoolOption("Auto Meow", "Automatically responds with a meow message whenever someone sends meow in chat.", defaults.automeow, () -> config.automeow, v -> config.automeow = v))
                                .option(createBoolOption("Meow Sounds", "Plays a cat sound whenever someone sends \"meow\" in chat", defaults.meowsounds, () -> config.meowsounds, v -> config.meowsounds = v))
                                .option(createBoolOption("Meow Death Sounds", "Plays a cat sound whenever an entity dies", defaults.meowdeathsounds, () -> config.meowdeathsounds, v -> config.meowdeathsounds = v))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Clean Chat"))
                                .option(createBoolOption("Clean join", "Replaces the guild and friend join messages with a cleaner version of them.", defaults.cleanjoin, () -> config.cleanjoin, v -> config.cleanjoin = v))
                                .option(createBoolOption("Clean messages", "Replaces the guild and friend chat messages with a cleaner version of them.", defaults.cleanmsg, () -> config.cleanmsg, v -> config.cleanmsg = v))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Slayers"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Slayers"))
                                .option(createBoolOption("Slayer timer", "Sends a message in your chat telling you how long it took to kill your boss.", defaults.slayertimer, () -> config.slayertimer, v -> config.slayertimer = v))
                                .option(createBoolOption("Slayer highlight", "Highlights your slayer boss.", defaults.slayerhighlight, () -> config.slayerhighlight, v -> config.slayerhighlight = v))
                                .build())
                        .build())
        )).generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, String desc, boolean defaultVal, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter, v -> {
                    setter.accept(v);
                    featManager.onConfigChange();
                })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> Text.literal(val ? "On" : "Off"))
                        .coloured(true))
                .build();
    }

    private static Option<Integer> createSliderOption(String name, String desc, int defaultVal, java.util.function.Supplier<Integer> getter, java.util.function.Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter, v -> {
                    setter.accept(v);
                    featManager.onConfigChange();
                })
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(0, 100)
                        .step(1)
                        .formatValue(val -> Text.literal(val + "%")))
                .build();
    }

    private static Option<String> createTextOption(String name, String desc, String defaultVal, java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter, v -> {
                    setter.accept(v);
                    featManager.onConfigChange();
                })
                .controller(opt -> StringControllerBuilder.create(opt))
                .build();
    }

    private static Option<java.awt.Color> createColorOption(String name, String desc, java.awt.Color defaultVal, java.util.function.Supplier<java.awt.Color> getter, java.util.function.Consumer<java.awt.Color> setter) {
        return Option.<java.awt.Color>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(desc)))
                .binding(defaultVal, getter, v -> {
                    setter.accept(v);
                    featManager.onConfigChange();
                })
                .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                .build();
    }
}