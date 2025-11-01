@file:Suppress("UNUSED")

package xyz.meowing.zen.events.core

import net.hypixel.data.region.Environment
import net.hypixel.data.type.ServerType
import xyz.meowing.knit.api.events.Event
import xyz.meowing.zen.api.dungeons.DungeonFloor
import xyz.meowing.zen.api.location.SkyBlockArea
import xyz.meowing.zen.api.location.SkyBlockIsland

sealed class LocationEvent {
    /**
     * Posted after a Hypixel server change has occurred.
     *
     * @see xyz.meowing.zen.api.hypixel.HypixelAPI
     * @since 1.2.0
     */
    class ServerChange(
        val name: String,
        val type: ServerType?,
        val lobby: String?,
        val mode: String?,
        val map: String?,
    ) : Event()

    /**
     * Posted after a Skyblock island change has occurred.
     *
     * @see xyz.meowing.zen.api.location.LocationAPI
     * @since 1.2.0
     */
    class IslandChange(
        val old: SkyBlockIsland?,
        val new: SkyBlockIsland?
    ) : Event()

    /**
     * Posted after a Skyblock area change has occurred.
     *
     * @see xyz.meowing.zen.api.location.LocationAPI
     * @since 1.2.0
     */
    class AreaChange(
        val old: SkyBlockArea,
        val new: SkyBlockArea
    ) : Event()

    /**
     * Posted after a Skyblock dungeon floor change has occurred.
     *
     * @see xyz.meowing.zen.api.location.LocationAPI
     * @since 1.2.0
     */
    class DungeonFloorChange(
        val new: DungeonFloor?
    ) : Event()

    /**
     * Posted after a world change has occurred.
     *
     * @see xyz.meowing.knit.api.events.EventBus
     * @since 1.2.0
     */
    class WorldChange : Event()

    /**
     * Posted after the client has received the ClientboundHelloPacket from Hypixel.
     *
     * @see xyz.meowing.zen.api.hypixel.HypixelAPI
     * @since 1.2.0
     */
    class HypixelJoin(
        val environment: Environment
    ) : Event() {
        val onAlpha: Boolean get() = environment == Environment.BETA
    }

    /**
     * Posted after the client has joined Skyblock.
     *
     * @see xyz.meowing.zen.api.location.LocationAPI
     * @since 1.2.0
     */
    class SkyblockJoin : Event()

    /**
     * Posted after the client has left Skyblock.
     *
     * @see xyz.meowing.zen.api.location.LocationAPI
     * @since 1.2.0
     */
    class SkyblockLeave : Event()
}