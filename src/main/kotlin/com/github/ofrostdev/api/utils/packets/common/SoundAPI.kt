package com.github.ofrostdev.api.utils.packets.common

import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import com.github.ofrostdev.api.utils.packets.PacketUtils
import kotlin.math.pow

@DslMarker
annotation class SoundDSL

@SoundDSL
class SoundAPI(private val soundName: String) {
    private var volume: Float = 1.0f
    private var pitch: Float = 1.0f
    private var location: Location? = null

    constructor(bukkitSound: Sound) : this(bukkitSound.name.lowercase().replace('_', '.'))

    fun volume(volume: Float) = apply { this.volume = volume }
    fun pitch(pitch: Float) = apply { this.pitch = pitch }
    fun location(location: Location) = apply { this.location = location }

    fun send(player: Player) {
        if (!player.isOnline) return
        val loc = location ?: player.location
        PacketUtils.sendPacket(player, buildPacket(loc))
    }

    fun sendTo(players: Collection<Player>) {
        if (players.isEmpty()) return
        if (location != null) {
            PacketUtils.sendPacket(players, buildPacket(location!!))
        } else {
            players.forEach { PacketUtils.sendPacket(it, buildPacket(it.location)) }
        }
    }

    fun sendToAll() {
        val loc = location ?: Bukkit.getOnlinePlayers().firstOrNull()?.location ?: return
        PacketUtils.sendPacketToAll(buildPacket(loc))
    }

    fun sendToArea(center: Location, radius: Double) {
        val squaredRadius = radius.pow(2)
        val playersInArea = Bukkit.getOnlinePlayers().filter { p ->
            p.location.world == center.world && squaredDistance(p.location, center) <= squaredRadius
        }
        if (playersInArea.isNotEmpty()) {
            PacketUtils.sendPacket(playersInArea, buildPacket(center))
        }
    }

    private fun squaredDistance(loc1: Location, loc2: Location): Double {
        return (loc1.x - loc2.x).pow(2) + (loc1.y - loc2.y).pow(2) + (loc1.z - loc2.z).pow(2)
    }

    private fun buildPacket(loc: Location): PacketPlayOutNamedSoundEffect =
        PacketPlayOutNamedSoundEffect(soundName, loc.x, loc.y, loc.z, volume, pitch)

    companion object {
        fun of(soundName: String) = SoundAPI(soundName)
        fun of(sound: Sound) = SoundAPI(sound)
    }
}

fun sound(name: String, block: SoundAPI.() -> Unit) {
    SoundAPI.of(name).apply(block)
}

fun sound(sound: Sound, block: SoundAPI.() -> Unit) {
    SoundAPI.of(sound).apply(block)
}
