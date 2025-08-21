package com.github.ofrostdev.api.utils.packets.animation

import com.github.ofrostdev.api.utils.packets.PacketUtils.sendPacket
import net.minecraft.server.v1_8_R3.*
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object AnimationUtil {
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    fun lerpLocation(a: Location, b: Location, t: Double): Location {
        val out = a.clone()
        out.x = a.x + (b.x - a.x) * t
        out.y = a.y + (b.y - a.y) * t
        out.z = a.z + (b.z - a.z) * t
        out.yaw = (a.yaw + (b.yaw - a.yaw) * t).toFloat()
        out.pitch = (a.pitch + (b.pitch - a.pitch) * t).toFloat()
        return out
    }

    private fun createEntityTeleportPacket(entityId: Int, loc: Location): Packet<*> {
        return PacketPlayOutEntityTeleport(
            entityId,
            MathHelper.floor(loc.x * 32.0),
            MathHelper.floor(loc.y * 32.0),
            MathHelper.floor(loc.z * 32.0),
            (loc.yaw * 256.0f / 360.0f).toInt().toByte(),
            (loc.pitch * 256.0f / 360.0f).toInt().toByte(),
            false
        )
    }

    private fun createEntityLookPacket(entityId: Int, yawDegrees: Float, pitchDegrees: Float): Packet<*> {
        val y = (yawDegrees * 256.0f / 360.0f).toInt().toByte()
        val p = (pitchDegrees * 256.0f / 360.0f).toInt().toByte()
        return PacketPlayOutEntityLook(entityId, y, p, true)
    }

    private fun createFakeArmorStandPacket(armorStand: EntityArmorStand): Packet<*> {
        return PacketPlayOutSpawnEntityLiving(armorStand)
    }

    private fun createEntityEquipmentPacket(entityId: Int, slot: Int, item: ItemStack): Packet<*> {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        return PacketPlayOutEntityEquipment(entityId, slot, nmsItem)
    }

    private fun createEntityPitchPacket(entity: Entity, pitchDegrees: Float): Packet<*> {
        val yawByte = (entity.yaw * 256.0f / 360.0f).toInt().toByte()
        val pitchByte = (pitchDegrees * 256.0f / 360.0f).toInt().toByte()
        return PacketPlayOutEntityLook(entity.id, yawByte, pitchByte, true)
    }

    fun sendEntityPitchToPlayers(entity: Entity, pitchDegrees: Float, players: Collection<Player?>?) {
        sendPacket(players, createEntityPitchPacket(entity, pitchDegrees))
    }

    fun sendFakeArmorStand(armorStand: EntityArmorStand, helmet: ItemStack, players: Collection<Player?>?) {
        val entityId = armorStand.id
        sendPacket(players, createFakeArmorStandPacket(armorStand))
        sendPacket(players, createEntityEquipmentPacket(entityId, 4, helmet))
    }

    fun sendEntityLookToPlayers(entityId: Int, yaw: Float, pitch: Float, players: Collection<Player?>?) {
        sendPacket(players, createEntityLookPacket(entityId, yaw, pitch))
    }

    fun sendEntityTeleportToPlayers(entityId: Int, loc: Location, players: Collection<Player?>?) {
        sendPacket(players, createEntityTeleportPacket(entityId, loc))
    }
}