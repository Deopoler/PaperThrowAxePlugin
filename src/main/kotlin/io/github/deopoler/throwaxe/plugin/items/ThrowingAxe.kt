package io.github.deopoler.throwaxe.plugin.items

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

object ThrowingAxe {
    val item: ItemStack = ItemStack(org.bukkit.Material.NETHERITE_AXE, 1)
    val meta: ItemMeta = item.itemMeta.apply {
        displayName(Component.text("Throwing Axe"))
        lore(
            listOf(
                Component.text("Right click to throw")
            )
        )
    }
    val range = 30.0  // 30 blocks
    val speed = 20.0 // 20 blocks/s
    val delay = 50L // 50ms delay
    val damage = 20.0 // 20 health

    init {
        item.itemMeta = meta
    }

    fun throwAxe(player: Player) {
        val vector = player.location.add(0.0,0.5,0.0).toVector().add(player.location.direction.multiply(range)).toLocation(player.world).subtract(player.location.add(0.0, 0.5, 0.0)).toVector()
        var dest = player.location.toVector().add(player.location.direction.multiply(range))
        val armorstand = player.world.spawnEntity(player.location.add(0.0, 0.5, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        armorstand.apply {
            setArms(true)
            setGravity(false)
            isVisible = false
            isSmall = true
            isMarker = true
            setItem(EquipmentSlot.HAND, ThrowingAxe.item)
            rightArmPose = EulerAngle(Math.toRadians(90.0), Math.toRadians(0.0), Math.toRadians(0.0))
        }

        player.inventory.itemInMainHand.amount -= 1

        HeartbeatScope().launch {
            val suspension = Suspension()
            while (true) {
                // rotation
                val rot = armorstand.rightArmPose.add(20.0, 0.0, 0.0)
                armorstand.rightArmPose = rot

                // move forward
                val totalSpeed = speed * delay / 1000.0
                armorstand.teleport(armorstand.location.add(vector.normalize().multiply(totalSpeed)))

                // check reach destination
                if (dest.toLocation(player.world).distance(armorstand.location) <= 1.0)
                {
                    armorstand.remove()
                    if (player.inventory.firstEmpty() != -1) {
                        player.inventory.addItem(item)
                    } else {
                        player.world.dropItemNaturally(player.location, item)
                    }
                    cancel()
                }

                // check entity collision
                for (entity in armorstand.location.chunk.entities) {
                    if (!armorstand.isDead) {
                        if (armorstand.location.distanceSquared(entity.location) < 1.5) {
                            if (entity != player && entity != armorstand) {
                                if (entity is LivingEntity) {
                                    armorstand.remove()
                                    entity.damage(damage, player)
                                    if (player.inventory.firstEmpty() != -1) {
                                        player.inventory.addItem(item)
                                    } else {
                                        player.world.dropItemNaturally(player.location, item)
                                    }
                                    cancel()
                                }
                            }
                        }


                    }
                }
                // check block collision
                if (!armorstand.isDead) {
                    val block = armorstand.getTargetBlockExact(1)
                    if (block != null && !block.isPassable) {

                        armorstand.remove()
                        if (player.inventory.firstEmpty() != -1) {
                            player.inventory.addItem(item)
                        } else {
                            player.world.dropItemNaturally(player.location, item)
                        }
                        cancel()
                    }
                }
                suspension.delay(delay)
            }
        }

    }


}