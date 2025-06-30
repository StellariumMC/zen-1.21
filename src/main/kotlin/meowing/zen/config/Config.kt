package meowing.zen.config

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import net.minecraft.client.gui.screen.Screen
import java.awt.Color

object ModMenuCompat {
    fun createConfigScreen(parent: Screen?): Screen = Zen.configUI
}

fun ZenConfig(): ConfigUI {
    return ConfigUI("ZenConfig")
        .addElement("General", "Custom model", ConfigElement(
            "worldage",
            "Send world age",
            "Sends world age to your chat.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Custom model", ConfigElement(
            "customsize",
            "Custom player model size",
            "Changes the size of your player model",
            ElementType.Switch(false)
        ))
        .addElement("General", "Custom model", ConfigElement(
            "customX",
            "Custom X",
            "X scale",
            ElementType.Slider(0.1, 5.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Custom model", ConfigElement(
            "customY",
            "Custom Y",
            "Y scale",
            ElementType.Slider(0.1, 5.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Custom model", ConfigElement(
            "customZ",
            "Custom Z",
            "Z scale",
            ElementType.Slider(0.1, 5.0, 1.0, true),
            { config -> config["customsize"] as? Boolean == true }
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlay",
            "Block overlay",
            "Custom block highlighting",
            ElementType.Switch(false)
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlaywidth",
            "Block overlay width",
            "Block overlay line width",
            ElementType.Slider(0.1, 5.0, 1.0, false),
            { config -> config["blockoverlay"] as? Boolean == true }
        ))
        .addElement("General", "Block overlay", ConfigElement(
            "blockoverlaycolor",
            "Block overlay color",
            "The color for Block overlay",
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["blockoverlay"] as? Boolean == true }
        ))

        .addElement("General", "Clean Chat", ConfigElement(
            "guildjoinleave",
            "Clean guild join/leave",
            "Replaces the guild and friend join messages with a cleaner version of them.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean Chat", ConfigElement(
            "friendjoinleave",
            "Clean friend join/leave",
            "Replaces the guild and friend join messages with a cleaner version of them.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean Chat", ConfigElement(
            "guildmessage",
            "Clean guild messages",
            "Replaces the guild chat messages with a cleaner version of them.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean Chat", ConfigElement(
            "partymessage",
            "Clean party messages",
            "Replaces the party chat messages with a cleaner version of them.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean Chat", ConfigElement(
            "betterah",
            "Better Auction house",
            "Better auction house messages.",
            ElementType.Switch(false)
        ))
        .addElement("General", "Clean Chat", ConfigElement(
            "betterbz",
            "Better Bazaar",
            "Better bazaar messages.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayertimer",
            "Slayer timer",
            "Sends a message in your chat telling you how long it took to kill your boss.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "slayerhighlight",
            "Slayer highlight",
            "Highlights your slayer boss.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "General", ConfigElement(
            "vengdmg",
            "Vengeance damager tracker",
            "Tracks and sends your vegeance damage in the chat.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Blaze", ConfigElement(
            "vengtimer",
            "Vengeance proc timer",
            "Time until vengeance procs.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Blaze", ConfigElement(
            "slayerstats",
            "Slayer stats",
            "Shows stats about your kill times",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrycounter",
            "Carry counter",
            "Counts and sends the carries that you do.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrybosshighlight",
            "Carry boss highlight",
            "Highlights your client's boss.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carrybosshighlightcolor",
            "Carry boss highlight color",
            "The color for boss highlight",
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["carrybosshighlight"] as? Boolean == true }
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryclienthighlight",
            "Carry client highlight",
            "Highlights your client's boss.",
            ElementType.Switch(false)
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryclienthighlightcolor",
            "Carry client highlight color",
            "The color for client highlight",
            ElementType.ColorPicker(Color(0, 255, 255, 127)),
            { config -> config["carryclienthighlight"] as? Boolean == true }
        ))
        .addElement("Slayers", "Carrying", ConfigElement(
            "carryvalue",
            "Carry value",
            "Carry values for the mod to automatically detect in a trade",
            ElementType.TextInput("", "Enter carry values...")
        ))
        .addElement("Meowing", "Auto meow", ConfigElement(
            "automeow",
            "Auto Meow",
            "Automatically responds with a meow message whenever someone sends meow in chat.",
            ElementType.Switch(false)
        ))
        .addElement("Meowing", "Meow Sounds", ConfigElement(
            "meowsounds",
            "Meow Sounds",
            "Plays a cat sound whenever someone sends \"meow\" in chat",
            ElementType.Switch(false)
        ))
        .addElement("Meowing", "Meow Sounds", ConfigElement(
            "meowdeathsounds",
            "Meow Death Sounds",
            "Plays a cat sound whenever an entity dies",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Blood helper", ConfigElement(
            "bloodtimer",
            "Blood camp helper",
            "Sends information related to blood camping.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Terminals", ConfigElement(
            "termtracker",
            "Terminal tracker",
            "Tracks the terminals/levers/devices that your party does.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Keys", ConfigElement(
            "keyalert",
            "Key spawn alert",
            null,
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Party finder", ConfigElement(
            "partyfindermsgs",
            "Party finder messages",
            "Custom party finder messages.",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Misc", ConfigElement(
            "serverlagtimer",
            "Server lag timer",
            "Amount of difference between the client ticks and the server ticks",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Crypt reminder", ConfigElement(
            "cryptreminder",
            "Crypt reminder",
            "Shows a notification about the current crypt count if all 5 aren't done",
            ElementType.Switch(false)
        ))
        .addElement("Dungeons", "Crypt reminder", ConfigElement(
            "cryptreminderdelay",
            "Crypt reminder delay",
            "Time in minutes",
            ElementType.Slider(1.0, 5.0, 2.0, false),
            { config -> config["cryptreminderdelay"] as? Boolean == true }
        ))
        .addElement("Dungeons", "Fire freeze", ConfigElement(
            "firefreeze",
            "Fire freeze timer",
            "Time until you should activate fire freeze",
            ElementType.Switch(false)
        ))
}