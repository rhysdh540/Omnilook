plugins {
    id("cosmicloom") version("1.1.1")
}

dependencies {
    cosmicReach(loom.cosmicReachClient("alpha", "0.4.6"))
    modImplementation(loom.cosmicQuilt("2.3.2"))
    implementation(rootProject.sourceSets["main"].output)
}

tasks.runClient {
    systemProperty("mixin.debug.export", "true")

    // these prevent the game from restarting, allowing us to attach the debugger without weird issues
    systemProperty("jvmIsRestarted", "true")
    jvmArgs("-XstartOnFirstThread")
}