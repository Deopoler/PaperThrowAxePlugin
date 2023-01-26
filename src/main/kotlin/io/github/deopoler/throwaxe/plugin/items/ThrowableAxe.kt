package io.github.deopoler.throwaxe.plugin.items

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.EulerAngle

object ThrowableAxe {
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
    val delay = 10L // 10ms delay
    val damage = 20.0 // 20 health

    init {
        item.itemMeta = meta
    }


    fun throwAxe(player: Player) {
        val startLocation = player.location.clone().add(0.0,0.5,0.0)
        val direction = player.location.direction.clone()
        val destLocation = startLocation.clone().add(direction.clone().multiply(range))
        val armorStand = player.world.spawnEntity(startLocation.clone(), EntityType.ARMOR_STAND) as ArmorStand
        armorStand.apply {
            setArms(true)
            setGravity(false)
            isVisible = false
            isSmall = true
            isMarker = true
            setItem(EquipmentSlot.HAND, item)
            rightArmPose = EulerAngle(Math.toRadians(90.0), Math.toRadians(0.0), Math.toRadians(0.0))
        }

        player.inventory.itemInMainHand.amount -= 1

        HeartbeatScope().launch {
            val suspension = Suspension()
            while (true) {
                // rotation
                val rot = armorStand.rightArmPose.add(20.0, 0.0, 0.0)
                armorStand.rightArmPose = rot

                // move forward
                val totalSpeed = speed * delay / 1000.0
                armorStand.teleport(armorStand.location.add(direction.clone().multiply(totalSpeed)))

                // check reach destination
                if (destLocation.clone().distance(armorStand.location.clone()) <= 1.0)
                {
                    armorStand.remove()
                    if (player.inventory.firstEmpty() != -1) {
                        player.inventory.addItem(item)
                    } else {
                        player.world.dropItemNaturally(player.location, item)
                    }
                    cancel()
                }

                // check entity collision
                for (entity in armorStand.location.chunk.entities) {
                    if (!armorStand.isDead) {
                        if (armorStand.location.distance(entity.location) < 1.0) {
                            if (entity != player && entity != armorStand) {
                                if (entity is LivingEntity) {
                                    armorStand.remove()
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
                if (!armorStand.isDead) {
                    val block = armorStand.getTargetBlockExact(1)
                    if (block != null && !block.isPassable) {

                        armorStand.remove()
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