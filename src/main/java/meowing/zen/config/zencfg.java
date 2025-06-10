package meowing.zen.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class zencfg {
    public static ConfigClassHandler<zencfg> Handler = ConfigClassHandler.createBuilder(zencfg.class)
            .id(net.minecraft.util.Identifier.of("meowing", "zen"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("zen.json5"))
                    .setJson5(true)
                    .build())
            .build();

    /////////////
    // Meowing //
    /////////////

    // Automeow //
    @SerialEntry
    public boolean automeow;

    public static Screen createConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(Handler, ((defaults, config, builder) -> builder
                .title(Text.literal("Zen Configuration"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Meowing"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Auto Meow"))
                                        .description(OptionDescription.of(Text.literal("Automatically responds with a meow message whenever someone sends meow in chat.")))
                                        .binding(defaults.automeow, () -> config.automeow, newVal -> config.automeow = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("Meowing")
                                                        : Text.literal("Not meowing")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}
