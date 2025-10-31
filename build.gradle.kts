import dev.deftu.gradle.utils.version.MinecraftVersions

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
    useDevAuth("1.2.1")
    useMixinRefMap(modData.id)
}

dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
    modImplementation(include("gg.essential:elementa:710")!!)
    modImplementation(include("gg.essential:universalcraft-${mcData}:430")!!)
    modImplementation(include("xyz.meowing:vexel-${mcData}:116")!!)

    when (mcData.version) {
        MinecraftVersions.VERSION_1_21_9 -> modImplementation("com.terraformersmc:modmenu:16.0.0-rc.1")
        MinecraftVersions.VERSION_1_21_7 -> modImplementation("com.terraformersmc:modmenu:15.0.0")
        MinecraftVersions.VERSION_1_21_5 -> modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
        else -> {}
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xlambdas=class")
    }
}

tasks.register("generateLists") {
    val srcDir = rootProject.file("src/main/kotlin/xyz/meowing/zen")
    val featureOutput = project.file("build/generated/resources/features.list")
    val commandOutput = project.file("build/generated/resources/commands.list")

    val moduleRegex = Regex("@Zen\\.Module\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
    val commandRegex = Regex("@Zen\\.Command\\s*(?:\\n|\\s)*(?:object|class)\\s+(\\w+)")
    val pkgRegex = Regex("package\\s+([\\w.]+)")

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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn("generateLists")
    from("build/generated/resources")
}

tasks.classes {
    dependsOn("generateLists")
}