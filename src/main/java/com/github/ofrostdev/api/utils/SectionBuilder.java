package com.github.ofrostdev.api.utils;

import com.github.ofrostdev.api.utils.items.ItemBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// fork by Frost
public class SectionBuilder<T> {

    private static final Map<Class<?>, Adapter<?>> CLASS_ADAPTERS = new HashMap<>();

    static {
        CLASS_ADAPTERS.put(ItemStack.class, new ItemAdapter());
        CLASS_ADAPTERS.put(String.class, new StringAdapter());
        CLASS_ADAPTERS.put(Material.class, new MaterialAdapter());
        CLASS_ADAPTERS.put(Location.class, new LocationAdapter());
        CLASS_ADAPTERS.put(StringList.class, new ListAdapter<>(new StringAdapter()));
        CLASS_ADAPTERS.put(ItemList.class, new ListAdapter<>(new ItemAdapter()));
        CLASS_ADAPTERS.put(Sound.class, new EnumAdapter<>(Sound.class));
        CLASS_ADAPTERS.put(EntityType.class, new EnumAdapter<>(EntityType.class));
    }

    private final ConfigurationSection mainSection;
    private final Class<T> clazz;
    private final Constructor<T> constructor;
    private final Map<String, Class<?>> parameters;
    private final Map<String, Adapter<?>> parametersAdapters;

    public static <E> SectionBuilder<E> of(Class<E> clazz, ConfigurationSection section) {
        return new SectionBuilder<E>(clazz, section);
    }

    private SectionBuilder(Class<T> clazz, ConfigurationSection mainSection) {
        this.mainSection = mainSection;
        this.clazz = clazz;
        this.constructor = (Constructor<T>) clazz.getConstructors()[0];
        this.parameters = new LinkedHashMap<>();
        this.parametersAdapters = new HashMap<>();
    }


    public SectionBuilder<T> parameter(String key, Class<?> valueClass) {
        this.parameters.put(key, valueClass);
        return this;
    }

    public SectionBuilder<T> parameter(String key, Class<?> valueClass, Adapter<?> adapter) {
        this.parameters.put(key, valueClass);
        this.parametersAdapters.put(key, adapter);
        return this;
    }

    public SectionBuilder<T> adapter(Class<?> clazz, Adapter<?> adapter) {
        CLASS_ADAPTERS.put(clazz, adapter);
        return this;
    }


    public List<T> build() {
        if (constructor.getParameterCount() - 1 != parameters.size()) { // primeiro argumento é sempre a key, e não precisa ser passado pelo método parameter(String, Class)
            throw new IllegalArgumentException("Constructor has " + (constructor.getParameterCount() - 1) + " parameters, but it was passed " + parameters.size() + " parameters.");
        }
        List<T> toReturn = new ArrayList<>();
        for (String key : mainSection.getKeys(false)) {
            try {
                ConfigurationSection section = mainSection.getConfigurationSection(key);
                List<Object> constructorParameters = new ArrayList<>();
                constructorParameters.add(key);
                for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
                    String parameter = entry.getKey();
                    Class<?> parameterClass = entry.getValue();
                    Object object = section.get(parameter);
                    Adapter<?> adapter = parametersAdapters.getOrDefault(parameter, CLASS_ADAPTERS.get(parameterClass));
                    constructorParameters.add(adapter == null ? object : adapter.supply(object));
                }
                T instance = constructor.newInstance(constructorParameters.toArray());
                toReturn.add(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return toReturn;
    }

    public <E> Map<E, T> build(Function<T, E> extractor) {
        return build().stream().collect(Collectors.toMap(extractor, Function.identity()));
    }

    public static interface Adapter<A> {
        A supply(Object object);
    }

    public static class ItemAdapter implements Adapter<ItemStack> {

        private final static MaterialAdapter MATERIAL_ADAPTER = new MaterialAdapter();
        private final static StringAdapter STRING_ADAPTER = new StringAdapter();

        /* O único atributo necessário é o material
            material: STONE
            #material: http://textures.minecraft.net/texture/fa8887814578ce7c540d7ab8cc6b2a2e22a7492cc86c65a7e839c887b2ed62 # caso tenha essa propriedade, o item automaticamente ira ser uma skull com a textura setada (pode ser um nick também)
            display-name: "&bFrost"
            amount: 1
            lore:
             - "&ePlugins"
            enchants:
              - DAMAGE_ALL:10
            nbts: # %uuid_random%
              - "key:value"
            glow: false
            dont-stack: true
         */
        @Override
        public ItemStack supply(Object object) {
            ConfigurationSection section = (ConfigurationSection) object;

            ItemBuilder ib;
            String materialStr = section.getString("material");
            int amount = section.isSet("amount") ? section.getInt("amount") : 1;
            String displayName = section.isSet("display-name") ? STRING_ADAPTER.supply(section.getString("display-name")) : null;
            List<String> lore = section.isSet("lore") ? section.getStringList("lore").stream().map(STRING_ADAPTER::supply).collect(Collectors.toList()) : null;
            boolean glow = section.isSet("glow") && section.getBoolean("glow");
            boolean dontstack = section.isSet("dont-stack") && section.getBoolean("dont-stack");
            Map<Enchantment, Integer> enchants = null;

            if (section.isSet("enchants")) {
                enchants = section.getStringList("enchants").stream()
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(a -> Enchantment.getByName(a[0]), a -> Integer.parseInt(a[1].trim())));
            }

            if (materialStr != null) {
                if (materialStr.startsWith("http://textures.minecraft.net/texture/") ||
                        materialStr.startsWith("https://textures.minecraft.net/texture/")) {
                    ib = new ItemBuilder(materialStr); // skull com textura
                } else {
                    Material material;
                    short data = 0;
                    if (materialStr.contains(":")) {
                        String[] split = materialStr.split(":");
                        try {
                            material = Material.valueOf(split[0].toUpperCase());
                            data = Short.parseShort(split[1]);
                        } catch (Exception e) {
                            material = Material.STONE;
                            data = 0;
                        }
                    } else {
                        try {
                            material = Material.valueOf(materialStr.toUpperCase());
                        } catch (Exception e) {
                            material = Material.STONE;
                        }
                    }
                    ib = new ItemBuilder(material, amount, data);
                }
            } else {
                ib = new ItemBuilder(Material.STONE, amount);
            }

            if (displayName != null) ib.setName(displayName);
            if (lore != null) ib.setLore(lore);
            if (glow) ib.setGlowing(true);
            if (dontstack) ib.setNBTString("frostkkj", UUID.randomUUID().toString());

            if (enchants != null)
                enchants.forEach(ib::addEnchant);

            if (section.isSet("nbts")) {
                List<String> nbtList = section.getStringList("nbts");
                for (String nbtEntry : nbtList) {
                    String[] split = nbtEntry.split(":", 2);
                    if (split.length != 2) continue;
                    ib.setNBTString(split[0].trim(), split[1].trim().replace("%uuid_random%", UUID.randomUUID().toString()));
                }
            }

            ib.setAmount(amount);
            return ib.build();
        }
    }

    private static class StringAdapter implements Adapter<String> {

        @Override
        public String supply(Object object) {
            return ChatColor.translateAlternateColorCodes('&', (String) object);
        }
    }

    private static class MaterialAdapter implements Adapter<Material> {

        @Override
        public Material supply(Object object) {
            String value = object.toString();
            return isNumber(value) ? Material.getMaterial(Integer.parseInt(value)) : Material.valueOf(value.toUpperCase());
        }

        private boolean isNumber(String string) {
            try {
                int i = Integer.parseInt(string);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

    }

    public static class ListAdapter<A> implements Adapter<List<A>> {

        private final Adapter<A> adapter;

        public ListAdapter(Adapter<A> adapter) {
            this.adapter = adapter;
        }

        @Override
        public List<A> supply(Object object) {
            if (object instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) object;
                return section.getKeys(false).stream().map(section::getConfigurationSection).map(adapter::supply).collect(Collectors.toList());
            } else {
                List<Object> list = (List<Object>) object;
                return list.stream().map(adapter::supply).collect(Collectors.toList());
            }
        }
    }

    public static class LocationAdapter implements Adapter<Location> {

        @Override
        public Location supply(Object object) {
            String value = (String) object;
            if (value == null || !value.contains(":")) return null;

            String[] parts = value.split(":");
            if (parts.length < 6) return null;

            World w = Bukkit.getServer().getWorld(parts[0]);
            if (w == null) return null;

            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);

            return new Location(w, x, y, z, yaw, pitch);
        }
    }

    public static class EnumAdapter<A extends Enum<A>> implements Adapter<A> {

        private final Class<A> enumClass;

        public EnumAdapter(Class<A> aEnum) {
            this.enumClass = aEnum;
        }

        @Override
        public A supply(Object object) {
            return Enum.valueOf(enumClass, (String) object);
        }
    }

    public static class ItemList {
    }

    public static class StringList {
    }
}
