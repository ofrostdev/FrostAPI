package com.github.ofrostdev.api.utils.common.fileconfiguration

import com.github.ofrostdev.api.utils.common.item.ItemBuilder
import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Constructor
import java.util.*
import kotlin.collections.LinkedHashMap

@DslMarker
annotation class SectionBuilderDSL

@SectionBuilderDSL
class KSectionBuilder<T>(clazz: Class<T>, section: ConfigurationSection) {

    private val builder: SectionBuilder<T> = SectionBuilder.of(clazz, section)

    fun parameter(key: String, type: Class<*>, adapter: SectionBuilder.Adapter<*>? = null) {
        if (adapter != null) builder.parameter(key, type, adapter) else builder.parameter(key, type)
    }

    fun adapter(clazz: Class<*>, adapter: SectionBuilder.Adapter<*>) = builder.adapter(clazz, adapter)

    fun build(): List<T> = builder.build()

    fun <E> buildMap(extractor: (T) -> E): Map<E, T> = builder.buildMap(extractor)
}

fun <T> section(clazz: Class<T>, section: ConfigurationSection, block: KSectionBuilder<T>.() -> Unit): List<T> {
    val kBuilder = KSectionBuilder(clazz, section)
    kBuilder.block()
    return kBuilder.build()
}

class SectionBuilder<T> private constructor(
    private val mainSection: ConfigurationSection,
    private val clazz: Class<T>
) {

    private val constructor: Constructor<T> = clazz.constructors[0] as Constructor<T>
    private val parameters: LinkedHashMap<String, Class<*>> = LinkedHashMap()
    private val parametersAdapters: MutableMap<String, Adapter<*>> = mutableMapOf()

    companion object {
        private val CLASS_ADAPTERS: MutableMap<Class<*>, Adapter<*>> = mutableMapOf(
            ItemStack::class.java to ItemAdapter(),
            String::class.java to StringAdapter(),
            Material::class.java to MaterialAdapter(),
            Location::class.java to LocationAdapter(),
            StringList::class.java to ListAdapter(StringAdapter()),
            ItemList::class.java to ListAdapter(ItemAdapter()),
            Sound::class.java to EnumAdapter(Sound::class.java),
            EntityType::class.java to EnumAdapter(EntityType::class.java),
            ListEffect::class.java to EffectAdapter()
        )

        fun <E> of(clazz: Class<E>, section: ConfigurationSection) = SectionBuilder<E>(section, clazz)
    }

    fun parameter(key: String, type: Class<*>): SectionBuilder<T> {
        parameters[key] = type
        return this
    }

    fun parameter(key: String, type: Class<*>, adapter: Adapter<*>): SectionBuilder<T> {
        parameters[key] = type
        parametersAdapters[key] = adapter
        return this
    }

    fun adapter(clazz: Class<*>, adapter: Adapter<*>): SectionBuilder<T> {
        CLASS_ADAPTERS[clazz] = adapter
        return this
    }

    fun build(): List<T> {
        if (constructor.parameterCount - 1 != parameters.size) {
            throw IllegalArgumentException("Constructor has ${constructor.parameterCount - 1} parameters, but ${parameters.size} provided.")
        }

        return mainSection.getKeys(false).mapNotNull { key ->
            try {
                val section = mainSection.getConfigurationSection(key)
                val args = mutableListOf<Any?>(key)
                parameters.forEach { (param, type) ->
                    val value = section?.get(param)
                    val adapter = parametersAdapters[param] ?: CLASS_ADAPTERS[type]
                    args.add(adapter?.supply(value) ?: value)
                }
                constructor.newInstance(*args.toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun <E> buildMap(extractor: (T) -> E): Map<E, T> = build().associateBy(extractor)

    interface Adapter<A> { fun supply(obj: Any?): A }

    class ItemAdapter : Adapter<ItemStack> {
        private val stringAdapter = StringAdapter()
        override fun supply(obj: Any?): ItemStack {
            val section = obj as ConfigurationSection
            val materialStr = section.getString("material")
            val amount = section.getInt("amount", 1)
            val displayName = section.getString("display-name")?.let { stringAdapter.supply(it) }
            val lore = section.getStringList("lore")?.map(stringAdapter::supply)
            val glow = section.getBoolean("glow", false)
            val dontStack = section.getBoolean("dont-stack", false)
            val enchants = section.getStringList("enchantments").associate {
                val (enchant, level) = it.split(":")
                Enchantment.getByName(enchant) to level.toInt()
            }.takeIf { it.isNotEmpty() }

            val ib = if (materialStr != null) {
                if (materialStr.startsWith("http")) ItemBuilder(materialStr)
                else {
                    val material = try { Material.valueOf(materialStr.uppercase()) } catch (_: Exception) { Material.STONE }
                    ItemBuilder(material, amount)
                }
            } else ItemBuilder(Material.STONE, amount)

            displayName?.let { ib.setName(it) }
            lore?.let { ib.setLore(it) }
            if (glow) ib.setGlowing(true)
            if (dontStack) ib.setNBTString("frostkkj", UUID.randomUUID().toString())
            enchants?.forEach { (ench, lvl) -> ib.addEnchantment(ench, lvl) }
            section.getStringList("nbts").forEach {
                val (k, v) = it.split(":", limit = 2)
                ib.setNBTString(k, v.replace("%uuid_random%", UUID.randomUUID().toString()))
            }
            return ib.build()
        }
    }

    class StringAdapter : Adapter<String> { override fun supply(obj: Any?) = ChatColor.translateAlternateColorCodes('&', obj as String) }
    class MaterialAdapter : Adapter<Material> { override fun supply(obj: Any?) = if (obj.toString().toIntOrNull() != null) Material.getMaterial(obj.toString().toInt()) else Material.valueOf(obj.toString().uppercase()) }
    class LocationAdapter : Adapter<Location?> {
        override fun supply(obj: Any?): Location? {
            val value = obj as? String ?: return null
            val parts = value.split(":")
            if (parts.size < 6) return null
            val world = Bukkit.getWorld(parts[0]) ?: return null
            val x = parts[1].toDoubleOrNull() ?: return null
            val y = parts[2].toDoubleOrNull() ?: return null
            val z = parts[3].toDoubleOrNull() ?: return null
            val yaw = parts[4].toFloatOrNull() ?: return null
            val pitch = parts[5].toFloatOrNull() ?: return null
            return Location(world, x, y, z, yaw, pitch)
        }
    }

    class EnumAdapter<A : Enum<A>>(private val enumClass: Class<A>) : Adapter<A> {
        override fun supply(obj: Any?) = java.lang.Enum.valueOf(enumClass, obj as String)
    }

    class ListAdapter<A>(private val adapter: Adapter<A>) : Adapter<List<A>> {
        override fun supply(obj: Any?): List<A> {
            return when (obj) {
                is ConfigurationSection -> obj.getKeys(false).map { adapter.supply(obj.getConfigurationSection(it)) }
                is List<*> -> obj.map(adapter::supply)
                else -> emptyList()
            }
        }
    }

    class EffectAdapter : Adapter<List<EffectAdapter.EffectData>> {
        class EffectData(val name: String, val amplifier: Int, val duration: Int)
        override fun supply(obj: Any?): List<EffectData> {
            return (obj as? List<*>)?.mapNotNull {
                val parts = (it as? String)?.split(":") ?: return@mapNotNull null
                if (parts.size != 3) return@mapNotNull null
                try { EffectData(parts[0].uppercase(), parts[1].toInt(), parts[2].toInt()) } catch (_: Exception) { null }
            } ?: emptyList()
        }
    }

    class ItemList
    class ListEffect
    class StringList
}
