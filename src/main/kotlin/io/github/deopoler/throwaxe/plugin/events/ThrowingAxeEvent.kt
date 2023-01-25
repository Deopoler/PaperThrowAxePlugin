package io.github.deopoler.throwaxe.plugin.events

import io.github.deopoler.throwaxe.plugin.items.ThrowingAxe
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.EulerAngle

object ThrowingAxeEvent: Listener {
    @EventHandler
    fun onInteract(event: PlayerInteractEvent)
    {
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)
        {
            val player = event.player
            val item = event.item
            if (item != null && item == ThrowingAxe.item)
            {
                ThrowingAxe.throwAxe(player)
            }
        }
    }
}