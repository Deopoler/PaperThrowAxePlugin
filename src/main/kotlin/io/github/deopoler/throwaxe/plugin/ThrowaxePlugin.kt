package io.github.deopoler.throwaxe.plugin

import io.github.deopoler.throwaxe.plugin.commands.GiveAxe
import io.github.deopoler.throwaxe.plugin.events.ThrowingAxeEvent
import org.bukkit.plugin.java.JavaPlugin

class ThrowaxePlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("Hello world!")
        getCommand("giveaxe")?.setExecutor(GiveAxe)
        server.pluginManager.registerEvents(ThrowingAxeEvent, this)
    }
}