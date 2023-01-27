package io.github.deopoler.throwaxe.items

import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.heartbeat.coroutines.Suspension
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.EulerAngle
import java.util.function.Predicate

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
    const val range = 30.0  // blocks
    const val speed = 20.0 // blocks/s
    const val delay = 50L // ms
    const val damage = 20.0 // health
    const val rotationSpeed = 1000.0 // degrees/s

    init {
        item.itemMeta = meta
    }


    fun throwAxe(player: Player) {
        val startLocation = player.location.clone().add(0.0, 0.5, 0.0)
        val direction = player.location.direction.clone()
        val destLocation = startLocation.clone().add(direction.clone().multiply(range))
        val armorStand = (player.world.spawnEntity(startLocation.clone(), EntityType.ARMOR_STAND) as ArmorStand)
            .apply {
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
                val rot = armorStand.rightArmPose.add(Math.toRadians(rotationSpeed * delay / 1000.0), 0.0, 0.0)
                armorStand.rightArmPose = rot

                // move forward
                val totalSpeed = speed * delay / 1000.0
                armorStand.teleport(armorStand.location.add(direction.clone().multiply(totalSpeed)))

                // check reach destination
                if (!armorStand.isDead) {
                    if (destLocation.clone().distance(armorStand.location.clone()) <= 1.0) {
                        armorStand.remove()
                        if (player.inventory.firstEmpty() != -1) {
                            player.inventory.addItem(item)
                        } else {
                            player.world.dropItemNaturally(player.location, item)
                        }
                        cancel()
                    }
                }

                // check collision
                if (!armorStand.isDead) {
                    player.world.rayTrace(
                        armorStand.location,
                        direction.clone(),
                        1.0,
                        FluidCollisionMode.NEVER,
                        true,
                        1.0,
                        Predicate { entity -> entity != player && entity != armorStand }
                    )?.let { result ->
                        result.hitEntity?.let { entity ->
                            if (entity is LivingEntity) {
                                entity.damage(damage, player)
                            }
                        }
                        if (player.inventory.firstEmpty() != -1) {
                            player.inventory.addItem(item)
                        } else {
                            player.world.dropItemNaturally(player.location, item)
                        }
                        armorStand.remove()
                        cancel()
                    }
                }
                suspension.delay(delay)
            }
        }
    }

}