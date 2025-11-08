@file:Suppress("UNUSED")

package xyz.meowing.zen.events

import xyz.meowing.knit.Knit
import xyz.meowing.knit.internal.events.WorldRenderEvent
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.util.ActionResult
import org.lwjgl.glfw.GLFW
import xyz.meowing.knit.api.events.Event
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.knit.internal.events.TickEvent
import xyz.meowing.vexel.Vexel
import xyz.meowing.zen.api.dungeons.DungeonAPI
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.LocationAPI
import xyz.meowing.zen.api.location.SkyBlockArea
import xyz.meowing.zen.api.location.SkyBlockIsland
import xyz.meowing.zen.events.core.ChatEvent
import xyz.meowing.zen.events.core.EntityEvent
import xyz.meowing.zen.events.core.GameEvent
import xyz.meowing.zen.events.core.GuiEvent
import xyz.meowing.zen.events.core.ItemTooltipEvent
import xyz.meowing.zen.events.core.LocationEvent
import xyz.meowing.zen.events.core.PacketEvent
import xyz.meowing.zen.events.core.RenderEvent
import xyz.meowing.zen.events.core.ServerEvent
import xyz.meowing.zen.managers.config.ConfigManager
import xyz.meowing.zen.managers.events.EventBusManager

object EventBus : xyz.meowing.knit.api.events.EventBus(true) {
    val messages = mutableListOf<String>()

    init {
        Knit.EventBus.register<WorldRenderEvent.Last> { event ->
            post(RenderEvent.World.Last(event.context))
        }

        Knit.EventBus.register<WorldRenderEvent.AfterEntities> { event ->
            post(RenderEvent.World.AfterEntities(event.context))
        }

        Knit.EventBus.register<WorldRenderEvent.BlockOutline> { event ->
            if (post(RenderEvent.World.BlockOutline(event.context))) event.cancel()
        }

        Knit.EventBus.register<TickEvent.Client.End> {
            post(xyz.meowing.zen.events.core.TickEvent.Client())
            TickScheduler.Client.onTick()
        }

        Knit.EventBus.register<TickEvent.Server.End> {
            post(xyz.meowing.zen.events.core.TickEvent.Server())
            TickScheduler.Server.onTick()
        }

        Vexel.eventBus.register<xyz.meowing.vexel.events.GuiEvent.Render> {
            post(GuiEvent.Render.NVG())
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            post(ServerEvent.Connect())
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            post(ServerEvent.Disconnect())
        }

        ClientLifecycleEvents.CLIENT_STARTED.register { _ ->
            post(GameEvent.Start())
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            post(GameEvent.Stop())
        }

        ClientEntityEvents.ENTITY_LOAD.register { entity, _ ->
            post(EntityEvent.Join(entity))
        }

        ClientEntityEvents.ENTITY_UNLOAD.register { entity, _ ->
            post(EntityEvent.Leave(entity))
        }

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            post(LocationEvent.WorldChange())
        }

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, isActionBar ->
            !post(ChatEvent.Receive(message, isActionBar))
        }

        ClientSendMessageEvents.ALLOW_CHAT.register { string ->
            val fromChatUtils = messages.remove(string)
            !post(ChatEvent.Send(string, fromChatUtils))
        }

        ClientSendMessageEvents.ALLOW_COMMAND.register { string ->
            val command = string.split(" ")[0].lowercase()
            val commandString = "/$string"
            val fromChatUtils = messages.remove(commandString)

            when (command) {
                "gc", "pc", "ac", "msg", "tell", "r", "say", "w", "reply" -> {
                    !post(ChatEvent.Send(commandString, fromChatUtils))
                }
                else -> true
            }
        }
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            //#if MC >= 1.21.9
            //$$ ScreenMouseEvents.allowMouseClick(screen).register { _, click ->
            //$$    !post(GuiEvent.Click(click.x, click.y, click.keycode, true, screen))
            //$$ }
            //$$
            //$$ ScreenMouseEvents.allowMouseRelease(screen).register { _, click ->
            //$$    !post(GuiEvent.Click(click.x, click.y, click.keycode, false, screen))
            //$$ }
            //$$
            //$$ ScreenKeyboardEvents.allowKeyPress(screen).register { _, keyInput ->
            //$$    val charTyped = GLFW.glfwGetKeyName(keyInput.key, keyInput.scancode)?.firstOrNull() ?: '\u0000'
            //$$    !post(GuiEvent.Key(GLFW.glfwGetKeyName(keyInput.key, keyInput.scancode), keyInput.key, charTyped, keyInput.key, screen))
            //$$ }
            //#else
            ScreenMouseEvents.allowMouseClick(screen).register { _, mx, my, mouseButton ->
                !post(GuiEvent.Click(mx, my, mouseButton, true, screen))
            }

            ScreenMouseEvents.allowMouseRelease(screen).register { _, mx, my, mouseButton ->
                !post(GuiEvent.Click(mx, my, mouseButton, false, screen))
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scancode, modifiers ->
                val charTyped = GLFW.glfwGetKeyName(key, scancode)?.firstOrNull() ?: '\u0000'
                !post(GuiEvent.Key(GLFW.glfwGetKeyName(key, scancode), key, charTyped, scancode, screen))
            }
            //#endif

            ScreenEvents.afterRender(screen).register { _, context, mouseX, mouseY, tickDelta ->
                post(GuiEvent.Render.HUD(context, GuiEvent.RenderType.Post))
            }
        }

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            if (screen != null) post(GuiEvent.Open(screen))
        }

        UseItemCallback.EVENT.register { player, world, hand ->
            post(EntityEvent.Interact(player, world, hand, "USE_ITEM"))
            ActionResult.PASS
        }

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "USE_BLOCK", hitResult.blockPos))
            ActionResult.PASS
        }

        UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "USE_ENTITY"))
            ActionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, hand, pos, direction ->
            post(EntityEvent.Interact(player, world, hand, "ATTACK_BLOCK", pos))
            ActionResult.PASS
        }

        AttackEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            post(EntityEvent.Interact(player, world, hand, "ATTACK_ENTITY"))
            ActionResult.PASS
        }

        ItemTooltipCallback.EVENT.register { stack, context, type, lines ->
            val tooltipEvent = ItemTooltipEvent(stack, context, type, lines)
            post(tooltipEvent)

            if (tooltipEvent.lines != lines) {
                lines.clear()
                lines.addAll(tooltipEvent.lines)
            }
        }
    }

    fun onPacketReceived(packet: Packet<*>): Boolean {
        if (post(PacketEvent.Received(packet))) return true

        return when (packet) {
            is EntitySpawnS2CPacket -> {
                post(EntityEvent.Packet.Spawn(packet))
            }
            else -> false
        }
    }

    fun onPacketSent(packet: Packet<*>) {
        post(PacketEvent.Sent(packet))
    }

    inline fun <reified T : Event> registerIn(
        vararg islands: SkyBlockIsland,
        skyblockOnly: Boolean = false,
        noinline callback: (T) -> Unit
    ) {
        val eventCall = register<T>(add = false, callback = callback)
        val islandSet = if (islands.isNotEmpty()) islands.toSet() else null
        EventBusManager.trackConditionalEvent(islandSet, skyblockOnly, eventCall)
    }
}

inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    island: Any? = null,
    area: Any? = null,
    dungeonFloor: Any? = null,
    noinline enabledCheck: (Map<String, Any?>) -> Boolean,
    noinline callback: (T) -> Unit
): EventCall {
    val eventCall = EventBus.register<T>(priority, false, callback)
    val keys = when (configKeys) {
        is String -> listOf(configKeys)
        is List<*> -> configKeys.filterIsInstance<String>()
        else -> throw IllegalArgumentException("configKeys must be String or List<String>")
    }

    val islands = when (island) {
        is SkyBlockIsland -> listOf(island)
        is List<*> -> island.filterIsInstance<SkyBlockIsland>()
        else -> emptyList()
    }

    val areas = when (area) {
        is SkyBlockArea -> listOf(area)
        is List<*> -> area.filterIsInstance<SkyBlockArea>()
        else -> emptyList()
    }

    val dungeonFloors = when (dungeonFloor) {
        is DungeonFloor -> listOf(dungeonFloor)
        is List<*> -> dungeonFloor.filterIsInstance<DungeonFloor>()
        else -> emptyList()
    }

    val checkAndUpdate = {
        val configValues = keys.associateWith { ConfigManager.getConfigValue(it) }
        val configEnabled = enabledCheck(configValues)
        val skyblockEnabled = !skyblockOnly || LocationAPI.isOnSkyBlock
        val islandEnabled = islands.isEmpty() || LocationAPI.island in islands
        val areaEnabled = areas.isEmpty() || LocationAPI.area in areas
        val dungeonFloorEnabled = if (dungeonFloors.isEmpty()) true else SkyBlockIsland.THE_CATACOMBS.inIsland() && DungeonAPI.dungeonFloor in dungeonFloors

        if (configEnabled && skyblockEnabled && islandEnabled && areaEnabled && dungeonFloorEnabled) {
            eventCall.register()
        } else {
            eventCall.unregister()
        }
    }

    keys.forEach { key ->
        ConfigManager.registerListener(key) { checkAndUpdate() }
    }

    if (islands.isNotEmpty()) {
        EventBus.register<LocationEvent.IslandChange> { checkAndUpdate() }
    }

    if (areas.isNotEmpty()) {
        EventBus.register<LocationEvent.AreaChange> { checkAndUpdate() }
    }

    if (dungeonFloors.isNotEmpty()) {
        EventBus.register<LocationEvent.DungeonFloorChange> { checkAndUpdate() }
    }

    if (skyblockOnly) {
        EventBus.register<LocationEvent.SkyblockJoin> { checkAndUpdate() }
        EventBus.register<LocationEvent.SkyblockLeave> { checkAndUpdate() }
    }

    checkAndUpdate()
    return eventCall
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    island: Any? = null,
    area: Any? = null,
    dungeonFloor: Any? = null,
    noinline callback: (T) -> Unit
): EventCall {
    return configRegister(configKeys, priority, skyblockOnly, island, area, dungeonFloor, { configValues ->
        configValues.values.all { it as? Boolean == true }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    enabledIndices: Set<Int>,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    island: Any? = null,
    area: Any? = null,
    dungeonFloor: Any? = null,
    noinline callback: (T) -> Unit
): EventCall {
    return configRegister(configKeys, priority, skyblockOnly, island, area, dungeonFloor, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Int -> value in enabledIndices
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}

@Suppress("UNUSED")
inline fun <reified T : Event> configRegister(
    configKeys: Any,
    requiredIndex: Int,
    priority: Int = 0,
    skyblockOnly: Boolean = false,
    island: Any? = null,
    area: Any? = null,
    dungeonFloor: Any? = null,
    noinline callback: (T) -> Unit
): EventCall {
    return configRegister(configKeys, priority, skyblockOnly, island, area, dungeonFloor, { configValues ->
        configValues.values.all { value ->
            when (value) {
                is Set<*> -> value.contains(requiredIndex)
                is Boolean -> value
                else -> false
            }
        }
    }, callback)
}