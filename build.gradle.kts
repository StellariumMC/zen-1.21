import dev.deftu.gradle.utils.version.MinecraftVersions
import dev.deftu.gradle.utils.includeOrShade

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    if (!mcData.isNeoForge) {
        useMixinRefMap(modData.id)
    }

    if (mcData.isForge) {
        useTweaker("org.spongepowered.asm.launch.MixinTweaker")
        useForgeMixin(modData.id)
    }

    if (mcData.isForgeLike && mcData.version >= MinecraftVersions.VERSION_1_16_5) {
        useKotlinForForge()
    }
}

dependencies {
    if (mcData.isFabric) {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
        modImplementation("gg.essential:elementa:710")
        shade("gg.essential:elementa:710")
        modImplementation("gg.essential:universalcraft-${mcData}:427")
        shade("gg.essential:universalcraft-${mcData}:427")
        modImplementation("org.reflections:reflections:0.10.2")
        includeOrShade("org.reflections:reflections:0.10.2")
        modImplementation("org.javassist:javassist:3.30.2-GA")
        includeOrShade("org.javassist:javassist:3.30.2-GA")

        if (mcData.version == MinecraftVersions.VERSION_1_21_7) {
            modImplementation("com.terraformersmc:modmenu:15.0.0-beta.3")
        } else if (mcData.version == MinecraftVersions.VERSION_1_21_5) {
            modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
        }
    } else if (mcData.version <= MinecraftVersions.VERSION_1_12_2) {
        implementation(includeOrShade(kotlin("stdlib-jdk8"))!!)
        implementation(includeOrShade("org.jetbrains.kotlin:kotlin-reflect:1.6.10")!!)

        modImplementation(includeOrShade("org.spongepowered:mixin:0.7.11-SNAPSHOT")!!)
    }
}

tasks {
    fatJar {
        if (mcData.isLegacyForge) {
            // yippee
        }
    }
}