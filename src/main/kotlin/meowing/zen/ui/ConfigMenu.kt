package meowing.zen.ui

import com.mojang.brigadier.context.CommandContext
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.general.ChatCleanerGui
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.TickUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import org.lwjgl.glfw.GLFW
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.core.VexelScreen
import xyz.meowing.vexel.elements.Button
import xyz.meowing.vexel.elements.Switch
import java.awt.Color

class ConfigMenu : VexelScreen() {
    var selectedCategory: String = "General"
    lateinit var featureArea: Rectangle
    // Store features in a structure: Category -> Feature -> Options

    override fun afterInitialization() {
        // Not an actual solution just filling up ui with mock data until all features are converted in their respective files
        ConfigMenuManager.addFeature("Architect Draft Message","", "Dungeons", ConfigElement(
            "architectdraft",
            ElementType.Switch(false)
        )).addFeatureOption("Only get drafts on your fails", description = "", element = ConfigElement(
            "selfdraft",
            ElementType.Switch(false)
        )).addFeatureOption("Automatically get architect drafts", description = "", element = ConfigElement(
            "autogetdraft",
            ElementType.Switch(false)
        ))

        ConfigMenuManager.addFeature("Slayer stats","Shows slayer statistics such as total bosses killed, bosses per hour, and average kill time. §c/slayerstats reset §rto reset stats. Requires §eSlayer Timer§r to be enabled.", "Slayers", ConfigElement(
            "slayerstats",
            ElementType.Switch(false)
        )).addFeatureOption("Only get drafts on your fails", element = ConfigElement(
            "slayerstatslines",
            ElementType.MultiCheckbox(
                options = listOf("Show Bosses Killed", "Show Bosses/hr", "Show Average kill time", "Show Average spawn time", "Show Total Session time", "Show XP/hr"),
                default = setOf(0, 1, 4, 5)
            )
        ))

        ConfigMenuManager.addFeature(
            "Chat Emotes",
            "Automatically replace emote codes with Unicode symbols in chat messages, example: <3 becomes ❤, use /emotes to see all supported emotes.",
            "General",
            ConfigElement(
                "chatemotes",
                ElementType.Switch(false)
            )
        )

        ConfigMenuManager.addFeature(
            "World age message",
            "Send world age",
            "General",
            ConfigElement(
                "worldage",
                ElementType.Switch(false)
            )
        )

        ConfigMenuManager.addFeature(
            "Chat Cleaner",
            "",
            "General",
            ConfigElement(
                "chatcleaner",
                ElementType.Switch(false)
            )
        ).addFeatureOption(
            "chatcleanerkey",
            "Keybind to add message to filter",
            "Options",
            ConfigElement(
                "chatcleanerkey",
                ElementType.Keybind(GLFW.GLFW_KEY_H)
            )
        ).addFeatureOption(
            "chatcleanergui",
            "Chat Cleaner Filter GUI",
            "GUI",
            ConfigElement(
                "chatcleanergui",
                ElementType.Button("Open Filter GUI") {
                    TickUtils.schedule(2) {
                        mc.setScreen(ChatCleanerGui())
                    }
                }
            )
        )

        ConfigMenuManager.addFeature(
            "Armor HUD",
            "",
            "HUD",
            ConfigElement(
                "armorhud",
                ElementType.Switch(false)
            )
        ).addFeatureOption(
            "armorhudvert",
            "Vertical Armor HUD",
            "Options",
            ConfigElement(
                "armorhudvert",
                ElementType.Switch(false)
            )
        ).addFeatureOption(
            "armorpieces",
            "Armor pieces to render",
            "Options",
            ConfigElement(
                "armorpieces",
                ElementType.MultiCheckbox(
                    listOf("Helmet", "Chestplate", "Leggings", "Boots"),
                    setOf(0, 1, 2, 3)
                )
            )
        )

        ConfigMenuManager.addFeature(
            "Auto meow",
            "Replies to messages in chat with a random meow",
            "Meowing",
            ConfigElement(
                "automeow",
                ElementType.Switch(false)
            )
        ).addFeatureOption(
            "automeowchannels",
            "Auto Meow Response Channels",
            "Options",
            ConfigElement(
                "automeowchannels",
                ElementType.MultiCheckbox(
                    options = listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"),
                    default = setOf(0, 1, 2, 3, 4)
                )
            )
        )

        ConfigMenuManager.addFeature(
            "Hide falling blocks",
            "Hide falling blocks",
            "QoL",
            ConfigElement(
                "hidefallingblocks",
                ElementType.Switch(false)
            )
        )

        ConfigMenuManager.addFeature(
            "Coherent rod",
            "Coherent rod radius display",
            "Rift",
            ConfigElement(
                "coherentrodoverlay",
                ElementType.Switch(false)
            )
        ).addFeatureOption(
            "coherentrodoverlaycolor",
            "Color",
            "Color",
            ConfigElement(
                "coherentrodoverlaycolor",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            )
        )

        ConfigMenuManager.addFeature(
            "Clean Chat",
            "Clean messages",
            "General",
            ConfigElement(
                "guildjoinleave",
                ElementType.Switch(false)
            )
        )

        setupUI()
    }

    override fun shouldPause(): Boolean = false

    private fun setupUI() {
        val base = Rectangle()
            .backgroundColor(0x80121212.toInt())
            .borderThickness(2f)
            .setSizing(70f, Size.ParentPerc, 65f, Size.ParentPerc)
            .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
            .scrollable(true)
            .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF45bffe.toInt())
            .borderRadius(12f)
            .dropShadow()
            .childOf(window)

        val sidebar = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(16f, Size.ParentPerc, 100f, Size.ParentPerc)
            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
            .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF45bffe.toInt())
            .borderThickness(2f)
            .borderRadius(12f)
            .dropShadow()
            .childOf(base)

        populateCategories(sidebar)

        featureArea = Rectangle()
            .backgroundColor(0x00000000)
            .setSizing(84f, Size.ParentPerc, 94f, Size.ParentPerc)
            .setPositioning(0f, Pos.AfterSibling, 6f, Pos.ParentPercent)
            .padding(20f)
            .scrollable(true)
            .childOf(base)

        populateFeatures()

//        val searchbar = Rectangle()
//            .backgroundColor(0x00000000)
//            .setSizing(90f, Size.ParentPerc, 6f, Size.ParentPerc)
//            .setPositioning(5f, Pos.ParentPixels, 5f, Pos.ParentPixels)
//            .borderThickness(2f)
//            .borderRadius(6f)
//            .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF45bffe.toInt())
//            .dropShadow()
//            .childOf(base)
    }

    fun populateCategories(sidebar: Rectangle) {
        val categories = ConfigMenuManager.categories

        for (category in categories) {
            val categoryElement = Rectangle()
                .backgroundColor(0x00000000.toInt())
                .padding(10f)
                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
                .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
                .borderRadius(5f)
                .childOf(sidebar)

            if(category == categories.first()) {
                categoryElement.setPositioning(0f, Pos.ParentPixels, 10f, Pos.ParentPercent)
            }

            val text = Text(category.name)
                .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
                .fontSize(25f)
                .color(0xFFAAAAAA.toInt())
                .childOf(categoryElement)

            categoryElement.onHover(
                onExit = { mouseX, mouseY ->
                    text.colorTo(0xFFAAAAAA.toInt(), 120, EasingType.EASE_OUT)
                    categoryElement.colorTo(0x00000000, 120, EasingType.EASE_OUT)
                },
                onEnter = {mouseX, mouseY ->
                    text.colorTo(0xFFFFFFFF.toInt(), 120, EasingType.EASE_OUT)
                    categoryElement.colorTo(0x20FFFFFF, 120, EasingType.EASE_OUT)
                }
            )

            // Add click event to load features of this category
            categoryElement.onClick { _, _, _ ->
                // Load features for this category
                println("Clicked on category: ${category.name}")
                selectedCategory = category.name
                populateFeatures()
                true
            }
        }
    }

    fun populateFeatures() {
        featureArea.children.clear()
        val category = ConfigMenuManager.categories.find { it.name == selectedCategory } ?: return

        category.features.forEach { feature ->
            val featureElement = Rectangle()
                .backgroundColor(0xFF000000.toInt())
                .padding(10f)
                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
                .setPositioning(0f, Pos.ParentPixels, 5f, Pos.AfterSibling)
                .borderRadius(5f)
                .borderThickness(2f)
                .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF062897.toInt())
                .childOf(featureArea)

            val text = Text(feature.featureName)
                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
                .fontSize(20f)
                .color(0xFFFFFFFF.toInt())
                .childOf(featureElement)

            createFeatureConfig(feature, featureElement)
        }
    }

    fun createFeatureConfig(feature: FeatureElement, featureElement: Rectangle) {
        if(feature.configElement.type is ElementType.Switch) {
            val switch = Switch()
                .childOf(featureElement)
                .setEnabled(value = true, animated = false, silent = true)

            featureElement.updateWidth()
            switch.setPositioning(featureElement.width - switch.width - 20f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        }
        if(feature.configElement.type is ElementType.Button) {
            val button = Button(text = feature.configElement.type.text)
                .childOf(featureElement)

            featureElement.updateWidth()
            button.setPositioning(featureElement.width - button.width - 20f, Pos.ParentPixels, 0f, Pos.ParentCenter)

            button.onClick { _, _, _ ->
                feature.configElement.type.onClick()
                true
            }
        }
    }
}

@Zen.Command
object CanvasCommand : CommandUtils("canvas") {
    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        TickUtils.schedule(2) {
            mc.setScreen(ConfigMenu())
        }
        return 1
    }
}

