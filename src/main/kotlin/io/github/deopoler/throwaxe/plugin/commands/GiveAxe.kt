package io.github.deopoler.throwaxe.plugin.commands

import io.github.deopoler.throwaxe.plugin.items.ThrowingAxe
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// /giveaxe
object GiveAxe:CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player)
        {
            sender.inventory.addItem(ThrowingAxe.item)
        }
        else{
            sender.sendMessage("Only player can run this command.")
        }

        return true
    }
}