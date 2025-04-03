pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.taumc.org/releases")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
        maven("https://jitpack.io")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "cosmicloom") {
                useModule("org.codeberg.CRModders:cosmic-loom:${requested.version}")
            }
        }
    }
}

include("cosmicreach")

rootProject.name = "Omnilook"
