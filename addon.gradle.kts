import net.darkhax.curseforgegradle.TaskPublishCurseForge

val dedicatedServerExcludedMods = listOf("Angelica", "ModularUI2", "Baubles-Expanded")

tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        // The GTNH setup adds dependencies to the classpath late, so remove
        // client-only mods immediately before launching a dedicated server.
        doFirst("stripClientOnlyMods") {
            classpath = classpath.filter { file ->
                dedicatedServerExcludedMods.none { mod -> file.name.contains(mod, ignoreCase = true) }
            }
        }
    }
}

tasks.withType<TaskPublishCurseForge>().configureEach {
    val publishTask = this
    doFirst("addCurseForgeSideMetadata") {
        publishTask.uploadArtifacts.forEach { artifact ->
            artifact.addEnvironment("Client", "Server")
        }
    }
}
