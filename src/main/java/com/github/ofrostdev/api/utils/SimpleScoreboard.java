package com.github.ofrostdev.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/*
    Copyright 2020 Braayy(github.com/Braayy)
    You can use and edit as much as you want since you keep this text on the source code.
 */

@SuppressWarnings({"unchecked", "unused"})
public class SimpleScoreboard {

    // region reflection cache
    private static Class<?> PACKET_PLAY_OUT_SCOREBOARD_OBJETIVE_CLASS;
    private static Class<?> PACKET_PLAY_OUT_SCOREBOARD_TEAM_CLASS;
    private static Class<?> PACKET_PLAY_OUT_SCOREBOARD_SCORE_CLASS;
    private static Class<?> PACKET_PLAY_OUT_SCOREBOARD_DISPLAY_OBJETIVE_CLASS;
    private static Object INTEGER;
    private static Object CHANGE;

    private static Method GET_HANDLE_METHOD;
    private static Method SEND_PACKET_METHOD;
    private static Field PLAYER_CONNECTION_FIELD;

    static {
        try {
            PACKET_PLAY_OUT_SCOREBOARD_OBJETIVE_CLASS = getNMSClass("PacketPlayOutScoreboardObjective");
            PACKET_PLAY_OUT_SCOREBOARD_TEAM_CLASS = getNMSClass("PacketPlayOutScoreboardTeam");
            PACKET_PLAY_OUT_SCOREBOARD_SCORE_CLASS = getNMSClass("PacketPlayOutScoreboardScore");
            PACKET_PLAY_OUT_SCOREBOARD_DISPLAY_OBJETIVE_CLASS = getNMSClass("PacketPlayOutScoreboardDisplayObjective");
            INTEGER = getNMSClass("IScoreboardCriteria").getClasses()[0].getDeclaredField("INTEGER").get(null);
            CHANGE = getNMSClass("PacketPlayOutScoreboardScore").getClasses()[0].getDeclaredField("CHANGE").get(null);

            GET_HANDLE_METHOD = Class.forName("org.bukkit.craftbukkit." + getNMSVersion() + ".entity.CraftPlayer").getDeclaredMethod("getHandle");
            SEND_PACKET_METHOD = getNMSClass("PlayerConnection").getDeclaredMethod("sendPacket", getNMSClass("Packet"));
            PLAYER_CONNECTION_FIELD = getNMSClass("EntityPlayer").getDeclaredField("playerConnection");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    // endregion

    /**
     * The title of the Scoreboard.
     */
    private final String title;
    /**
     * The lines of the Scoreboard.
     */
    private final String[] lines;

    public SimpleScoreboard(String title) {
        this.title = title;
        this.lines = new String[21];
    }

    /**
     * Sets the line number {line} to {text}.
     * @param line The line number to be set.
     * @param text The text to the line number.
     */
    public void setLine(int line, String text) {
        line = Math.abs(line);

        if (line > 21) {
            throw new IllegalArgumentException("This lib only supports 21 lines of scoreboard.");
        }

        if (text.length() > 32) {
            throw new IllegalArgumentException("This lib only supports 32 characters in a line.");
        }

        this.lines[line] = text;
    }

    /**
     * Gets the text of a line number.
     * @param line The line number to be get.
     * @return The text of that line number.
     */
    public String getLine(int line) {
        line = Math.abs(line);

        if (line > 21) {
            throw new IllegalArgumentException("This lib only supports 21 lines of scoreboard.");
        }

        return this.lines[line];
    }

    /**
     * Shows the Scoreboard to the {player}.
     * @param player The player who the Scoreboard will be show to.
     */
    public void show(Player player) {
        try {
            final Object packObjective = createObjectivePacket();
            sendPacket(player, packObjective);

            for (int lineNumber = 0; lineNumber < this.lines.length; lineNumber++) {
                final String text = this.lines[lineNumber];
                if (text != null) {
                    final String playerName = ChatColor.values()[lineNumber].toString() + ChatColor.RESET;
                    final String teamName = "l" + lineNumber;

                    final Object packTeam = createTeamPacket(teamName, text, playerName, true);
                    sendPacket(player, packTeam);

                    final Object packScore = createScorePacket(playerName, lineNumber);
                    sendPacket(player, packScore);
                }
            }

            final Object packDisplay = createDisplayPacket();
            sendPacket(player, packDisplay);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Updates the Scoreboard with the new text of the {lines}.
     * @param player The player who the Scoreboard will be updated to.
     */
    public void update(Player player) {
        try {
            for (int lineNumber = 0; lineNumber < this.lines.length; lineNumber++) {
                final String line = this.lines[lineNumber];
                if (line != null) {
                    final String teamName = "l" + lineNumber;

                    final Object packTeam = createTeamPacket(teamName, line, null, false);
                    sendPacket(player, packTeam);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Update title of scoreboard and send packet to player
     * @param player O jogador.
     * @param newTitle O novo título.
     */
    public void updateTitle(Player player, String newTitle) {
        try {
            final Object packObjective = PACKET_PLAY_OUT_SCOREBOARD_OBJETIVE_CLASS.newInstance();
            setFieldValue(packObjective, "a", newTitle);
            setFieldValue(packObjective, "b", newTitle);
            setFieldValue(packObjective, "c", INTEGER);
            setFieldValue(packObjective, "d", 2);
            sendPacket(player, packObjective);

            final Object packDisplay = PACKET_PLAY_OUT_SCOREBOARD_DISPLAY_OBJETIVE_CLASS.newInstance();
            setFieldValue(packDisplay, "a", 1);
            setFieldValue(packDisplay, "b", newTitle);
            sendPacket(player, packDisplay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if line is empty
     * @param lineNumber line
     * @return true if empty.
     */
    public boolean isLineEmpty(int lineNumber) {
        if (lineNumber < 0 || lineNumber >= lines.length) return true;
        return lines[lineNumber] == null || lines[lineNumber].trim().isEmpty();
    }

    /**
     * Update the specified line and send packet to player
     * @param player The player.
     * @param lineNumber The line of update.
     */
    public void updateLine(Player player, int lineNumber) {
        try {
            if (lineNumber < 0 || lineNumber >= lines.length) return;
            String line = lines[lineNumber];
            if (line != null) {
                String teamName = "l" + lineNumber;
                String playerName = ChatColor.values()[lineNumber].toString() + ChatColor.RESET;

                Object packTeam = createTeamPacket(teamName, line, null, false);
                sendPacket(player, packTeam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all lines internally without atualizar o scoreboard.
     */
    public void clearLines() {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = null;
        }
    }

    /**
     * Clears the scoreboard for the player by removing the objective.
     * @param player The player whose scoreboard will be cleared.
     */
    public void clear(Player player) {
        try {
            final Object packObjective = PACKET_PLAY_OUT_SCOREBOARD_OBJETIVE_CLASS.newInstance();

            setFieldValue(packObjective, "a", this.title);
            setFieldValue(packObjective, "b", this.title);
            setFieldValue(packObjective, "c", INTEGER);
            setFieldValue(packObjective, "d", 1);

            sendPacket(player, packObjective);

            final Object packDisplay = PACKET_PLAY_OUT_SCOREBOARD_DISPLAY_OBJETIVE_CLASS.newInstance();
            setFieldValue(packDisplay, "a", 1);
            setFieldValue(packDisplay, "b", "");
            sendPacket(player, packDisplay);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the max of line is supported.
     * @return Max lines (21)
     */
    public int getMaxLines() {
        return lines.length;
    }

    /**
     * Creates the objective packet with the {this.title} title.
     * @return The objective packet
     * @throws Exception If something goes wrong.
     */
    private Object createObjectivePacket() throws Exception {
        final Object packObjective = PACKET_PLAY_OUT_SCOREBOARD_OBJETIVE_CLASS.newInstance();
        setFieldValue(packObjective, "a", this.title);
        setFieldValue(packObjective, "b", this.title);
        setFieldValue(packObjective, "c", INTEGER);
        setFieldValue(packObjective, "d", 0);

        return packObjective;
    }

    /**
     * Creates the team packet with that {text} as a prefix-suffix mix.
     * @param teamName The team name to be created.
     * @param text The text to be displayed.
     * @param playerName The player name associated with that team.
     * @param addEntry If is should add the player name to the list.
     * @return The team packet
     * @throws Exception If something goes wrong.
     */
    private Object createTeamPacket(String teamName, String text, String playerName, boolean addEntry) throws Exception {
        final Object packTeam = PACKET_PLAY_OUT_SCOREBOARD_TEAM_CLASS.newInstance();
        setFieldValue(packTeam, "a", teamName);
        setFieldValue(packTeam, "h", 0);
        setFieldValue(packTeam, "b", teamName);
        if (text.length() > 16) {
            setFieldValue(packTeam, "c", text.substring(0, 16));
            setFieldValue(packTeam, "d", text.substring(16));
        } else {
            setFieldValue(packTeam, "c", "");
            setFieldValue(packTeam, "d", text);
        }
        setFieldValue(packTeam, "i", 0);
        setFieldValue(packTeam, "e", "always");
        setFieldValue(packTeam, "f", -1);

        if (addEntry) {
            final Field field = packTeam.getClass().getDeclaredField("g");
            field.setAccessible(true);

            List<String> playerList = (List<String>) field.get(packTeam);
            playerList.add(playerName);
        }

        return packTeam;
    }

    /**
     * Creates the score packet with the player name associated with the team on the line number.
     * @param playerName The player name associated with the team.
     * @param lineNumber The line number where it should be displayed.
     * @return The score packet
     * @throws Exception If something goes wrong.
     */
    private Object createScorePacket(String playerName, int lineNumber) throws Exception {
        final Object packScore = PACKET_PLAY_OUT_SCOREBOARD_SCORE_CLASS.newInstance();
        setFieldValue(packScore, "a", playerName);
        setFieldValue(packScore, "b", this.title);
        setFieldValue(packScore, "c", lineNumber);
        setFieldValue(packScore, "d", CHANGE);

        return packScore;
    }

    /**
     * Creates the display packet, it just display all that content above.
     * @return The display packet
     * @throws Exception If something goes wrong.
     */
    private Object createDisplayPacket() throws Exception {
        final Object packDisplay = PACKET_PLAY_OUT_SCOREBOARD_DISPLAY_OBJETIVE_CLASS.newInstance();
        setFieldValue(packDisplay, "a", 1);
        setFieldValue(packDisplay, "b", this.title);

        return packDisplay;
    }

    /**
     * Sends a packet to a player.
     * @param player The player that the packet will be sent.
     * @param packet The packet that will be sent.
     * @throws Exception If something goes wrong.
     */
    private static void sendPacket(Player player, Object packet) throws Exception {
        final Object handle = GET_HANDLE_METHOD.invoke(player);
        final Object playerConnection = PLAYER_CONNECTION_FIELD.get(handle);

        SEND_PACKET_METHOD.invoke(playerConnection, packet);
    }

    /**
     * @param className The NMS Class name.
     * @return The NMS Class associated with that {className}.
     * @throws Exception If it doesn't find that class.
     */
    private static Class<?> getNMSClass(String className) throws Exception{
        return Class.forName("net.minecraft.server." + getNMSVersion() + "." + className);
    }

    /**
     * @return The NMS version that the server is running at.
     */
    private static String getNMSVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    /**
     * Sets the value of a field in the {obj} object to {fieldValue}
     * @param obj The object to be modified.
     * @param fieldName The field name.
     * @param fieldValue The field value.
     * @throws Exception If something goes wrong.
     */
    private static void setFieldValue(Object obj, String fieldName, Object fieldValue) throws Exception {
        final Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        field.set(obj, fieldValue);
    }
}