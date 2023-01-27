package io.github.deopoler.throwaxe.plugin

import io.github.deopoler.throwaxe.commands.GiveThrowableAxe
import io.github.deopoler.throwaxe.events.ThrowingAxeEvent
import org.bukkit.plugin.java.JavaPlugin

class ThrowaxePlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("Hello world!")

        getCommand("giveaxe")?.setExecutor(GiveThrowableAxe)
        server.pluginManager.registerEvents(ThrowingAxeEvent, this)

    }
}