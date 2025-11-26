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

repositories {
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.teamresourceful.com/repository/maven-public/")
}

val clocheAction: Action<ExternalModuleDependency> = Action {
    attributes {
        attribute(Attribute.of("earth.terrarium.cloche.modLoader", String::class.java), "fabric")
        attribute(Attribute.of("earth.terrarium.cloche.minecraftVersion", String::class.java),
            when (mcData.version) {
                MinecraftVersions.VERSION_1_21_10 -> "1.21.9"
                else -> mcData.toString().substringBefore("-")
            }
        )
    }
}

dependencies {
    implementation(include("io.github.classgraph:classgraph:4.8.184")!!)

    modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")

    modImplementation(include("xyz.meowing:vexel-${mcData}:125")!!)

    modImplementation(include("net.hypixel:mod-api:1.0.1")!!)
    modImplementation(include("maven.modrinth:hypixel-mod-api:1.0.1+build.1+mc1.21")!!)

    modImplementation("me.owdding:item-data-fixer:1.0.5", clocheAction)
    modImplementation("tech.thatgravyboat:skyblock-api:3.0.23") {
        exclude("me.owdding")
        clocheAction.execute(this)
    }
    include("tech.thatgravyboat:skyblock-api:3.0.23", clocheAction)

    when (mcData.version) {
        MinecraftVersions.VERSION_1_21_10 -> modImplementation("com.terraformersmc:modmenu:16.0.0-rc.1")
        MinecraftVersions.VERSION_1_21_8 -> modImplementation("com.terraformersmc:modmenu:15.0.0")
        MinecraftVersions.VERSION_1_21_5 -> modImplementation("com.terraformersmc:modmenu:14.0.0-rc.2")
        else -> {}
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xlambdas=class")
    }
}