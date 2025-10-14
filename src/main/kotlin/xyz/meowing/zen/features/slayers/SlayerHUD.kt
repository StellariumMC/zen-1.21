package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.SkyblockEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Render2D.width
import xyz.meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.Entity

@Zen.Module
object SlayerHUD : Feature("slayerhud", true) {
    private const val name = "Slayer HUD"
    private var timerEntity: Entity? = null
    private var hpEntity: Entity? = null
    private var bossID: Int? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        xyz.meowing.zen.ui.ConfigMenuManager
            .addFeature("Slayer HUD", "Slayer HUD", "Slayers", xyz.meowing.zen.ui.ConfigElement(
                "slayerhud",
                ElementType.Switch(false)
            ))
        return configUI
    }


    override fun initialize() {
        HUDManager.register(name, "§c02:59\n§c☠ §bVoidgloom Seraph IV §e64.2M§c❤")

        createCustomEvent<RenderEvent.HUD>("render") {
            if (HUDManager.isEnabled(name)) render(it.context)
        }

        register<SkyblockEvent.Slayer.Spawn> { event ->
            val world = world ?: return@register
            bossID = event.entityID
            timerEntity = world.getEntityById(event.entityID - 1)
            hpEntity = world.getEntityById(event.entityID - 2)
            registerEvent("render")
        }

        register<SkyblockEvent.Slayer.Death> {
            unregisterEvent("render")
            bossID = null
        }

        register<SkyblockEvent.Slayer.Fail> {
            unregisterEvent("render")
            bossID = null
        }

        register<SkyblockEvent.Slayer.Cleanup> {
            unregisterEvent("render")
            bossID = null
        }
    }

    private fun render(context: DrawContext) {
        val time = timerEntity?.name?.string ?: return
        val hp = hpEntity?.name?.string ?: return
        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val matrices = context.matrices

        //#if MC >= 1.21.7
        //$$ matrices.pushMatrix()
        //$$ matrices.translate(x, y)
        //#else
        matrices.push()
        matrices.translate(x, y, 0f)
        //#endif
        val hpWidth = hp.removeFormatting().width()
        val timeWidth = time.removeFormatting().width()
        Render2D.renderStringWithShadow(context, time, (hpWidth - timeWidth) / 2f, 0f, scale)
        Render2D.renderStringWithShadow(context, hp, 0f, 10f, scale)
        //#if MC >= 1.21.7
        //$$ matrices.popMatrix()
        //#else
        matrices.pop()
        //#endif
    }
}