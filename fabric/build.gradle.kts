plugins {
    id("net.fabricmc.fabric-loom") version "1.15.5"
    id("spectatorplus.platform")
}

description = "A Fabric mod that improves spectator mode by showing the hotbar, health, and held item of the spectated player"

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("spectatorplus") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("client"))
        }
    }

    runs {
        getByName("client") {
            client()
            ideConfigGenerated(true)
            runDir("run/client")
        }

        getByName("server") {
            server()
            ideConfigGenerated(true)
            runDir("run")
        }
    }

    accessWidenerPath = file("src/main/resources/spectatorplus.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    include(implementation("me.lucko:fabric-permissions-api:${property("fabric_permissions_api_version")}")!!)

    implementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    implementation("com.terraformersmc:modmenu:${property("modmenu_version")}")

    include(implementation("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")!!)
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:${property("mixinextras_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to project.version,
                    "description" to project.description,
                )
            )
        }
    }

    jar {
        from("../LICENSE")
    }


}
