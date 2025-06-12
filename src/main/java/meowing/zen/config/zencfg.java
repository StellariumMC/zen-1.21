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
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Auto Meow"))
                                        .description(OptionDescription.of(Text.literal("Automatically responds with a meow message whenever someone sends meow in chat.")))
                                        .binding(defaults.automeow, () -> config.automeow, newVal -> config.automeow = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Meow Sounds"))
                                        .description(OptionDescription.of(Text.literal("Plays a cat sound whenever someone sends \"meow\" in chat")))
                                        .binding(defaults.meowsounds, () -> config.meowsounds, newVal -> config.meowsounds = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Meow Death Sounds"))
                                        .description(OptionDescription.of(Text.literal("Plays a cat sound whenever an entity dies")))
                                        .binding(defaults.meowdeathsounds, () -> config.meowdeathsounds, newVal -> config.meowdeathsounds = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Clean Chat"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Clean join"))
                                        .description(OptionDescription.of(Text.literal("Replaces the guild and friend join messages with a cleaner version of them.")))
                                        .binding(defaults.cleanjoin, () -> config.cleanjoin, newVal -> config.cleanjoin = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Clean messages"))
                                        .description(OptionDescription.of(Text.literal("Replaces the guild and friend chat messages with a cleaner version of them.")))
                                        .binding(defaults.cleanmsg, () -> config.cleanmsg, newVal -> config.cleanmsg = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Slayers"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Slayers"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Slayer timer"))
                                        .description(OptionDescription.of(Text.literal("Sends a message in your chat telling you how long it took to kill your boss.")))
                                        .binding(defaults.slayertimer, () -> config.slayertimer, newVal -> config.slayertimer = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Slayer highlight"))
                                        .description(OptionDescription.of(Text.literal("Highlights your slayer boss.")))
                                        .binding(defaults.slayerhighlight, () -> config.slayerhighlight, newVal -> config.slayerhighlight = newVal)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val
                                                        ? Text.literal("On")
                                                        : Text.literal("Off")
                                                )
                                                .coloured(true)
                                        )
                                        .build())
                                .build())
                        .build())
        )).generateScreen(parent);
    }
}
