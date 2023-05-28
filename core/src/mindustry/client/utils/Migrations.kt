package mindustry.client.utils

import arc.*
import arc.Core.*
import arc.input.*
import arc.struct.*
import arc.util.*
import mindustry.input.*
import mindustry.type.*

@Suppress("unused")
/** Allows for the easy */
class Migrations {
    fun runMigrations() {
        val funs = this::class.java.declaredMethods // Cached function list
        var migration = settings.getInt("foomigration", 1) // Starts at 1
        while (true) {
            val migrateFun = funs.find { it.name == "migration$migration" } ?: break // Find next migration or break
            Log.debug("Running foo's migration $migration")
            migrateFun.isAccessible = true
            migrateFun.invoke(this)
            migrateFun.isAccessible = false
            Log.debug("Finished running foo's migration $migration")
            migration++
        }
        if (settings.getInt("foomigration", 1) != migration) settings.put("foomigration", migration) // Avoids saving settings if the value remains the same
    }

    private fun migration1() { // All of the migrations from before the existence of the migration system
        // Old settings that no longer exist
        settings.remove("drawhitboxes")
        settings.remove("signmessages")
        settings.remove("firescl")
        settings.remove("effectscl")
        settings.remove("commandwarnings")
        settings.remove("nodeconfigs")
        settings.remove("attemwarfarewhisper")

        // Various setting names and formats have changed
        if (settings.has("gameovertext")) {
            if (settings.getString("gameovertext").isNotBlank()) settings.put("gamewintext", settings.getString("gameovertext"))
            settings.remove("gameovertext")
        }
        if (settings.has("graphdisplay")) {
            if (settings.getBool("graphdisplay")) settings.put("highlighthoveredgraph", true)
            settings.remove("graphdisplay")
        }
        if (settings.getBool("drawhitboxes") && settings.getInt("hitboxopacity") == 0) { // Old setting was enabled and new opacity hasn't been set yet
            settings.put("hitboxopacity", 30)
            UnitType.hitboxAlpha = settings.getInt("hitboxopacity") / 100f
        }
    }

    private fun migration2() { // Lowercased the pingExecutorThreads setting name1
        if (!settings.has("pingExecutorThreads")) return
        settings.put("pingexecutorthreads", settings.getInt("pingExecutorThreads"))
        settings.remove("pingExecutorThreads")
    }

    private fun migration3() { // Finally changed Binding.navigate_to_camera to navigate_to_cursor
        InputDevice.DeviceType.values().forEach { device ->
            if (!settings.has("keybind-default-$device-navigate_to_camera-key")) return@forEach
            val saved = settings.getInt("keybind-default-$device-navigate_to_camera-key")
            settings.remove("keybind-default-$device-navigate_to_camera-key")
            settings.remove("keybind-default-$device-navigate_to_camera-single")
            keybinds.sections.first { it.name == "default" }.binds[device, ::OrderedMap].put(
                Binding.navigate_to_cursor,
                KeyBinds.Axis(KeyCode.byOrdinal(saved))
            )
        }
    }

    private fun migration4() = settings.remove("broadcastcoreattack") // Removed as it was super annoying

    private fun migration5() = settings.remove("disablemonofont") // Removed as it was made irrelevant long ago
}