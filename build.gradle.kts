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

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.polyfrost.org/releases")
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
        modImplementation(includeOrShade("gg.essential:elementa:710")!!)
        modImplementation(includeOrShade("gg.essential:universalcraft-${mcData}:427")!!)
        modImplementation(includeOrShade("org.reflections:reflections:0.10.2")!!)
        modImplementation(includeOrShade("org.javassist:javassist:3.30.2-GA")!!)

        if (mcData.version == MinecraftVersions.VERSION_1_21_7) {
            modImplementation("com.terraformersmc:modmenu:15.0.0-beta.3")
            modImplementation(includeOrShade("xyz.meowing:vexel-1.21.7-fabric:1.0.0")!!)
        } else if (mcData.version == MinecraftVersions.VERSION_1_21_5) {
            modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
            modImplementation(includeOrShade("xyz.meowing:vexel-1.21.5-fabric:1.0.0")!!)
        }

        runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
    } else if (mcData.version <= MinecraftVersions.VERSION_1_12_2) {
        implementation(includeOrShade(kotlin("stdlib-jdk8"))!!)
        implementation(includeOrShade("org.jetbrains.kotlin:kotlin-reflect:1.6.10")!!)

        modImplementation(includeOrShade("org.spongepowered:mixin:0.7.11-SNAPSHOT")!!)
    }
}

val moduleRegex = Regex("@Zen\\.Module\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
val commandRegex = Regex("@Zen\\.Command\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
val pkgRegex = Regex("package\\s+([\\w.]+)")

tasks.register("generateLists") {
    val srcDir = rootProject.file("src/main/kotlin/meowing/zen")
    val featureOutput = rootProject.file("src/main/resources/features.list")
    val commandOutput = rootProject.file("src/main/resources/commands.list")

    inputs.dir(srcDir).optional(true)
    outputs.files(featureOutput, commandOutput)

    doLast {
        val featureClasses = mutableListOf<String>()
        val commandClasses = mutableListOf<String>()

        if (!srcDir.exists()) return@doLast

        srcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension in listOf("kt", "java")) {
                val text = file.readText()
                val pkg = pkgRegex.find(text)?.groupValues?.get(1) ?: return@forEach

                moduleRegex.findAll(text).forEach { match ->
                    featureClasses += "${pkg}.${match.groupValues[1]}"
                }

                commandRegex.findAll(text).forEach { match ->
                    commandClasses += "${pkg}.${match.groupValues[1]}"
                }
            }
        }

        featureOutput.parentFile.mkdirs()
        commandOutput.parentFile.mkdirs()
        featureOutput.writeText(featureClasses.joinToString("\n"))
        commandOutput.writeText(commandClasses.joinToString("\n"))
    }
}

tasks.processResources {
    dependsOn("generateLists")
}

tasks.classes {
    dependsOn("generateLists")
}

tasks.findByName("preprocessResources")?.dependsOn(":1.21.5-fabric:generateLists")
tasks.findByName("preprocessResources")?.dependsOn("generateLists")