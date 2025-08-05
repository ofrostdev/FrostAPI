package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builder para facilitar o envio de mensagens no ActionBar em servidores Spigot 1.8.8.
 * Suporta mensagens com cores, placeholders e envio temporizado.
 *
 * <p>Uso:</p>
 * <pre>{@code
 * ActionBarBuilder.of("&aOlá &b%player%!")
 *     .replace("%player%", player.getName())
 *     .colorize()
 *     .sound(Sound.VILLAGER_YES)
 *     .send(player);
 * }</pre>
 *
 * @author Alvaro Borges (Cyranox)
 */
public final class ActionBar {

    private String message;
    private Sound sound = null;

    private ActionBar(String message) {
        this.message = message;
    }

    /**
     * Cria uma nova instância do builder com a mensagem fornecida.
     *
     * @param message Texto base com ou sem cores.
     * @return Instância do builder.
     */
    public static ActionBar of(String message) {
        return new ActionBar(message);
    }

    /**
     * Substitui um texto alvo por outro.
     *
     * @param target Texto a ser substituído.
     * @param replacement Novo texto.
     * @return Instância atualizada do builder.
     */
    public ActionBar replace(String target, String replacement) {
        if (target != null && replacement != null) {
            message = message.replace(target, replacement);
        }
        return this;
    }

    /**
     * Converte os códigos de cores (&) para (§) utilizados no Minecraft.
     *
     * @return Instância atualizada do builder.
     */
    public ActionBar colorize() {
        message = message.replaceAll("(?i)&([0-9a-fk-or])", "§$1");
        return this;
    }

    /**
     * Define um som a ser tocado ao enviar a ActionBar.
     *
     * @param sound Som do pacote {@link Sound}.
     * @return Instância atualizada do builder.
     */
    public ActionBar sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Envia a mensagem ao jogador.
     *
     * @param player Jogador alvo.
     */
    public void send(Player player) {
        if (player == null || !player.isOnline()) return;

        IChatBaseComponent component = new ChatComponentText(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }

    }

    /**
     * Envia a mensagem para vários jogadores.
     *
     * @param players Coleção de jogadores.
     */
    public void sendTo(Collection<? extends Player> players) {
        players.forEach(this::send);
    }

    /**
     * Envia para todos os jogadores online.
     */
    public void broadcast() {
        sendTo(Bukkit.getOnlinePlayers());
    }

    /**
     * Envia a mensagem ao jogador por vários segundos.
     *
     * @param player Jogador alvo.
     * @param plugin Plugin executor.
     * @param seconds Duração em segundos.
     */
    public void sendFor(Player player, Plugin plugin, int seconds) {
        if (player == null || plugin == null || seconds <= 0) return;
        runRepeatedly(plugin, seconds, () -> send(player));
    }

    public static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    /**
     * Envia a mensagem no estilo "máquina de escrever" (caractere por caractere) no ActionBar.
     *
     * @param plugin Plugin principal.
     * @param player Jogador alvo.
     * @param text Mensagem a ser escrita.
     * @param delay Delay entre os caracteres (em ticks, 20 ticks = 1 segundo).
     */
    public static void sendTypewriter(Plugin plugin, Player player, String text, int delay) {
        if (plugin == null || player == null || text == null || text.isEmpty()) return;

        UUID uuid = player.getUniqueId();

        if (activeTasks.containsKey(uuid)) {
            BukkitTask previousTask = activeTasks.remove(uuid);
            if (previousTask != null) previousTask.cancel();
        }

        String translatedText = ChatColor.translateAlternateColorCodes('&', text);

        BukkitTask task = new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activeTasks.remove(uuid);
                    return;
                }

                if (index < translatedText.length()) {
                    String partial = translatedText.substring(0, index + 1);
                    ActionBar.of(partial).send(player);
                    index++;
                } else {
                    this.cancel();
                    activeTasks.remove(uuid);
                }
            }
        }.runTaskTimer(plugin, 0L, delay);

        activeTasks.put(uuid, task);
    }

    /**
     * Envia a mensagem a todos online por vários segundos.
     *
     * @param plugin Plugin executor.
     * @param seconds Duração em segundos.
     */
    public void broadcastFor(Plugin plugin, int seconds) {
        if (plugin == null || seconds <= 0) return;
        runRepeatedly(plugin, seconds, this::broadcast);
    }

    private void runRepeatedly(Plugin plugin, int seconds, Runnable action) {
        new BukkitRunnable() {
            int count = seconds;
            @Override
            public void run() {
                if (count-- <= 0) cancel();
                else action.run();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}