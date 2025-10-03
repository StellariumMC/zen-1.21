package xyz.meowing.zen.ui

//import com.mojang.brigadier.context.CommandContext
//import xyz.meowing.zen.Zen
//import xyz.meowing.zen.Zen.Companion.mc
//import xyz.meowing.zen.config.ui.types.ElementType
//import xyz.meowing.zen.features.general.ChatCleanerGui
//import xyz.meowing.zen.utils.CommandUtils
//import xyz.meowing.zen.utils.TickUtils
//import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
//import org.lwjgl.glfw.GLFW
//import xyz.meowing.vexel.animations.EasingType
//import xyz.meowing.vexel.animations.colorTo
//import xyz.meowing.vexel.components.base.Pos
//import xyz.meowing.vexel.components.base.Size
//import xyz.meowing.vexel.components.core.Rectangle
//import xyz.meowing.vexel.components.core.SvgImage
//import xyz.meowing.vexel.components.core.Text
//import xyz.meowing.vexel.core.VexelScreen
//import xyz.meowing.vexel.elements.Button
//import xyz.meowing.vexel.elements.Switch
//import java.awt.Color
//
//class ConfigMenu : VexelScreen() {
//    var selectedCategory: String = "General"
//    lateinit var featureArea: Rectangle
//    // Store features in a structure: Category -> Feature -> Options
//
//    override fun afterInitialization() {
//        // Not an actual solution just filling up ui with mock data until all features are converted in their respective files
//        ConfigMenuManager.addFeature("Architect Draft Message","", "Dungeons", ConfigElement(
//            "architectdraft",
//            ElementType.Switch(false)
//        )).addFeatureOption("Only get drafts on your fails", description = "", element = ConfigElement(
//            "selfdraft",
//            ElementType.Switch(false)
//        )).addFeatureOption("Automatically get architect drafts", description = "", element = ConfigElement(
//            "autogetdraft",
//            ElementType.Switch(false)
//        ))
//
//        ConfigMenuManager.addFeature("Slayer stats","Shows slayer statistics such as total bosses killed, bosses per hour, and average kill time. §c/slayerstats reset §rto reset stats. Requires §eSlayer Timer§r to be enabled.", "Slayers", ConfigElement(
//            "slayerstats",
//            ElementType.Switch(false)
//        )).addFeatureOption("Only get drafts on your fails", element = ConfigElement(
//            "slayerstatslines",
//            ElementType.MultiCheckbox(
//                options = listOf("Show Bosses Killed", "Show Bosses/hr", "Show Average kill time", "Show Average spawn time", "Show Total Session time", "Show XP/hr"),
//                default = setOf(0, 1, 4, 5)
//            )
//        ))
//
//        ConfigMenuManager.addFeature(
//            "Chat Emotes",
//            "Automatically replace emote codes with Unicode symbols in chat messages, example: <3 becomes ❤, use /emotes to see all supported emotes.",
//            "General",
//            ConfigElement(
//                "chatemotes",
//                ElementType.Switch(false)
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "World age message",
//            "Send world age",
//            "General",
//            ConfigElement(
//                "worldage",
//                ElementType.Switch(false)
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Chat Cleaner",
//            "",
//            "General",
//            ConfigElement(
//                "chatcleaner",
//                ElementType.Switch(false)
//            )
//        ).addFeatureOption(
//            "chatcleanerkey",
//            "Keybind to add message to filter",
//            "Options",
//            ConfigElement(
//                "chatcleanerkey",
//                ElementType.Keybind(GLFW.GLFW_KEY_H)
//            )
//        ).addFeatureOption(
//            "chatcleanergui",
//            "Chat Cleaner Filter GUI",
//            "GUI",
//            ConfigElement(
//                "chatcleanergui",
//                ElementType.Button("Open Filter GUI") {
//                    TickUtils.schedule(2) {
//                        mc.setScreen(ChatCleanerGui())
//                    }
//                }
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Armor HUD",
//            "",
//            "HUD",
//            ConfigElement(
//                "armorhud",
//                ElementType.Switch(false)
//            )
//        ).addFeatureOption(
//            "armorhudvert",
//            "Vertical Armor HUD",
//            "Options",
//            ConfigElement(
//                "armorhudvert",
//                ElementType.Switch(false)
//            )
//        ).addFeatureOption(
//            "armorpieces",
//            "Armor pieces to render",
//            "Options",
//            ConfigElement(
//                "armorpieces",
//                ElementType.MultiCheckbox(
//                    listOf("Helmet", "Chestplate", "Leggings", "Boots"),
//                    setOf(0, 1, 2, 3)
//                )
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Auto meow",
//            "Replies to messages in chat with a random meow",
//            "Meowing",
//            ConfigElement(
//                "automeow",
//                ElementType.Switch(false)
//            )
//        ).addFeatureOption(
//            "automeowchannels",
//            "Auto Meow Response Channels",
//            "Options",
//            ConfigElement(
//                "automeowchannels",
//                ElementType.MultiCheckbox(
//                    options = listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"),
//                    default = setOf(0, 1, 2, 3, 4)
//                )
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Hide falling blocks",
//            "Hide falling blocks",
//            "QoL",
//            ConfigElement(
//                "hidefallingblocks",
//                ElementType.Switch(false)
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Coherent rod",
//            "Coherent rod radius display",
//            "Rift",
//            ConfigElement(
//                "coherentrodoverlay",
//                ElementType.Switch(false)
//            )
//        ).addFeatureOption(
//            "coherentrodoverlaycolor",
//            "Color",
//            "Color",
//            ConfigElement(
//                "coherentrodoverlaycolor",
//                ElementType.ColorPicker(Color(0, 255, 255, 127))
//            )
//        )
//
//        ConfigMenuManager.addFeature(
//            "Clean Chat",
//            "Clean messages",
//            "General",
//            ConfigElement(
//                "guildjoinleave",
//                ElementType.Switch(false)
//            )
//        )
//
//        setupUI()
//    }
//
//    override fun shouldPause(): Boolean = false
//
//    private fun setupUI() {
//        val base = Rectangle()
//            .backgroundColor(0x80121212.toInt())
//            .borderThickness(2f)
//            .setSizing(70f, Size.ParentPerc, 65f, Size.ParentPerc)
//            .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
//            .scrollable(true)
//            .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF45bffe.toInt())
//            .borderRadius(12f)
//            .dropShadow()
//            .childOf(window)
//
//        val sidebar = Rectangle()
//            .backgroundColor(0x00000000)
//            .setSizing(16f, Size.ParentPerc, 100f, Size.ParentPerc)
//            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
//            .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF45bffe.toInt())
//            .borderThickness(2f)
//            .borderRadius(12f)
//            .dropShadow()
//            .childOf(base)
//
//        val categories = Rectangle()
//            .backgroundColor(0x00000000)
//            .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//            .setPositioning(0f, Pos.ParentPixels, 10f, Pos.ParentPercent)
//            .childOf(sidebar)
//
//        reloadCategories(categories)
//
//        featureArea = Rectangle()
//            .backgroundColor(0x00000000)
//            .setSizing(84f, Size.ParentPerc, 94f, Size.ParentPerc)
//            .setPositioning(0f, Pos.AfterSibling, 6f, Pos.ParentPercent)
//            .padding(20f)
//            .scrollable(true)
//            .childOf(base)
//
//        populateFeatures()
//
//        val searchbar = Rectangle()
//            .backgroundColor(0x00000000)
//            .setSizing(70f, Size.ParentPerc, 4f, Size.ParentPerc)
//            .setPositioning(24f, Pos.ParentPercent, 20f, Pos.ParentPixels)
//            .borderThickness(2f)
//            .borderRadius(6f)
//            .borderColor(0xFF7b7b84.toInt())
//            .dropShadow()
//            .childOf(base)
//    }
//
//    var lastSelectedElement: Rectangle? = null
//    fun reloadCategories(categoriesElement: Rectangle) {
//        val categories = ConfigMenuManager.categories
//
//        for (category in categories) {
//            val unselectedBackground = 0x00000000
//            val unselectedTextColor = 0xFFAAAAAA.toInt()
//            val selectedBackground = 0x20FFFFFF
//            val selectedTextColor = 0xFFFFFFFF.toInt()
//
//            val categoryElement = Rectangle()
//                .backgroundColor(unselectedBackground)
//                .padding(10f)
//                .setSizing(80f, Size.ParentPerc, 0f, Size.Auto)
//                .setPositioning(0f, Pos.ParentCenter, 5f, Pos.AfterSibling)
//                .borderRadius(13f)
//                .childOf(categoriesElement)
//
//            val text = Text(category.name)
//                .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
//                .fontSize(25f)
//                .color(unselectedTextColor)
//                .childOf(categoryElement)
//
//            // Highlight if selected for the first time loading
//            if(selectedCategory == category.name) {
//                categoryElement.backgroundColor = selectedBackground
//                text.textColor = selectedTextColor
//                lastSelectedElement = categoryElement
//            }
//
//            categoryElement.onHover(
//                onExit = { mouseX, mouseY ->
//                    if(selectedCategory == category.name) return@onHover
//                    text.colorTo(0xFFAAAAAA.toInt(), 120, EasingType.EASE_OUT)
//                    categoryElement.colorTo(0x00000000, 120, EasingType.EASE_OUT)
//                },
//                onEnter = {mouseX, mouseY ->
//                    if(selectedCategory == category.name) return@onHover
//                    text.colorTo(0xFFFFFFFF.toInt(), 120, EasingType.EASE_OUT)
//                    categoryElement.colorTo(0x20FFFFFF, 120, EasingType.EASE_OUT)
//                }
//            )
//
//            // Add click event to load features of this category
//            categoryElement.onClick { _, _, _ ->
//                // Remove highlight from last selected
//                lastSelectedElement?.let { (lastSelectedElement as Rectangle).backgroundColor = 0x00000000; (it.children[0] as Text).textColor = 0xFFAAAAAA.toInt() }
//
//                // Highlight this element
//                categoryElement.backgroundColor = selectedBackground
//                text.textColor = selectedTextColor
//
//                selectedCategory = category.name
//                lastSelectedElement = categoryElement
//
//                // Populate features for this category
//                populateFeatures()
//                true
//            }
//        }
//    }
//
//    fun populateFeatures() {
//        featureArea.children.clear()
//        val category = ConfigMenuManager.categories.find { it.name == selectedCategory } ?: return
//
//        category.features.forEach { feature ->
//            val featureElement = Rectangle()
//                .padding(20f)
//                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                .borderRadius(8f)
//                .borderThickness(3f)
//                .setGradientBorderColor(0xFF052846.toInt(), 0xFF093463.toInt())
//                .setBackgroundGradientColor(0xFF001534.toInt(), 0xFF00050c.toInt())
//                .childOf(featureArea)
//                .dropShadow()
//
//            val text = Text(feature.featureName)
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .fontSize(24f)
//                .color(0xFF007acc.toInt())
//                .childOf(featureElement)
//
//            if(feature.options.isNotEmpty()) {
//                val optionsIcon = SvgImage(svgPath = "/assets/zen/options.svg", color = Color.GRAY)
//                    .setSizing(40f, Size.Pixels, 40f, Size.Pixels)
//                    .setPositioning(80f, Pos.ParentPercent, 0f, Pos.ParentCenter)
//                    .childOf(featureElement)
//            }
//
//            createFeatureConfig(feature, featureElement)
//        }
//    }
//
//    fun createFeatureConfig(feature: FeatureElement, featureElement: Rectangle) {
//        if(feature.configElement.type is ElementType.Switch) {
//            val switch = Switch(thumbWidth = 27f, thumbHeight = 27f)
//                .childOf(featureElement)
//                .thumbColor(0xFF042e57.toInt())
//                .thumbDisabledColor(0xFF042e57.toInt())
//                .trackDisabledColor(0xFF00050c.toInt())
//                .trackEnabledColor(0xFF00050c.toInt())
//                .borderThickness(2f)
//                .borderRadius(19f)
//                .borderColor(0xFF042e57.toInt())
//                .setSizing(80f, Size.Pixels, 35f, Size.Pixels)
//                .setEnabled(value = true, animated = false, silent = true)
//
//            featureElement.updateWidth()
//            switch.setPositioning(featureElement.width - switch.width - 40f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//            return
//        }
//        if(feature.configElement.type is ElementType.Button) {
//            val button = Button(text = feature.configElement.type.text)
//                .childOf(featureElement)
//
//            featureElement.updateWidth()
//            button.setPositioning(featureElement.width - button.width - 40f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//
//            button.onClick { _, _, _ ->
//                feature.configElement.type.onClick()
//                true
//            }
//            return
//        }
//    }
//}
//
//@Zen.Command
//object CanvasCommand : CommandUtils("canvas") {
//    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
//        TickUtils.schedule(2) {
//            mc.setScreen(ConfigMenu())
//        }
//        return 1
//    }
//}

