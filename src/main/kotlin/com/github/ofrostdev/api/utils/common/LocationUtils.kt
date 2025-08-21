package com.github.ofrostdev.api.utils.common

import com.github.ofrostdev.api.utils.common.fileconfiguration.SectionBuilder
import org.bukkit.Location
import kotlin.math.max
import kotlin.math.min

object LocationUtils {

    private val LOCATION_ADAPTER = SectionBuilder.LocationAdapter()

    class Builder(private val loc: Location?) {
        fun isSameBlockAs(other: Location?): Boolean {
            if (loc == null || other == null) return false
            return loc.world == other.world &&
                    loc.blockX == other.blockX &&
                    loc.blockY == other.blockY &&
                    loc.blockZ == other.blockZ
        }

        fun isBetween(corner1: Location?, corner2: Location?): Boolean {
            if (loc == null || corner1 == null || corner2 == null) return false
            if (loc.world != corner1.world || loc.world != corner2.world) return false

            val x1 = min(corner1.x, corner2.x)
            val x2 = max(corner1.x, corner2.x)
            val y1 = min(corner1.y, corner2.y)
            val y2 = max(corner1.y, corner2.y)
            val z1 = min(corner1.z, corner2.z)
            val z2 = max(corner1.z, corner2.z)

            return loc.x in x1..x2 && loc.y in y1..y2 && loc.z in z1..z2
        }

        fun distanceTo(other: Location?): Double {
            if (loc == null || other == null) return -1.0
            if (loc.world != other.world) return -1.0
            return loc.distance(other)
        }

        fun serialize(): String? {
            if (loc == null || loc.world == null) return null
            return "${loc.world.name}:${loc.x}:${loc.y}:${loc.z}:${loc.yaw}:${loc.pitch}"
        }

        fun deserialize(input: String?): Location? {
            if (input.isNullOrEmpty()) return null
            return LOCATION_ADAPTER.supply(input)
        }
    }

    fun dsl(loc: Location?, block: Builder.() -> Unit): Builder {
        val builder = Builder(loc)
        builder.block()
        return builder
    }
}
