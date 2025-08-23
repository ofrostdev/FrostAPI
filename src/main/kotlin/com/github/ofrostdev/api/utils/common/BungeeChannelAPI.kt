package com.github.ofrostdev.api.utils.common

import com.google.common.collect.Iterables
import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.Messenger
import org.bukkit.plugin.messaging.PluginMessageListener
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction
import kotlin.collections.HashMap

class BungeeChannelAPI private constructor(private val plugin: Plugin) {

    companion object {
        private val registeredInstances = WeakHashMap<Plugin, BungeeChannelAPI>()

        @Synchronized
        fun of(plugin: Plugin): BungeeChannelAPI {
            return registeredInstances.compute(plugin) { _, v ->
                if (v == null) BungeeChannelAPI(plugin) else v
            }!!
        }
    }

    private val callbackMap: MutableMap<String, Queue<CompletableFuture<*>>> = HashMap()
    private var forwardListeners: MutableMap<String, ForwardConsumer>? = null
    private var globalForwardListener: ForwardConsumer? = null
    private val messageListener: PluginMessageListener

    init {
        val messenger = Bukkit.getServer().messenger
        messenger.registerOutgoingPluginChannel(plugin, "BungeeCord")
        messageListener = PluginMessageListener { channel, player, message ->
            onPluginMessageReceived(channel, player, message)
        }
        messenger.registerIncomingPluginChannel(plugin, "BungeeCord", messageListener)
    }

    fun registerForwardListener(globalListener: ForwardConsumer) {
        globalForwardListener = globalListener
    }

    fun registerForwardListener(channelName: String, listener: ForwardConsumer) {
        if (forwardListeners == null) forwardListeners = HashMap()
        synchronized(forwardListeners!!) {
            forwardListeners!![channelName] = listener
        }
    }

    fun getPlayerCount(serverName: String): CompletableFuture<Int> {
        val player = getFirstPlayer()
        val future = CompletableFuture<Int>()
        synchronized(callbackMap) {
            callbackMap.compute("PlayerCount-$serverName", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("PlayerCount")
        output.writeUTF(serverName)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun getPlayerList(serverName: String): CompletableFuture<List<String>> {
        val player = getFirstPlayer()
        val future = CompletableFuture<List<String>>()
        synchronized(callbackMap) {
            callbackMap.compute("PlayerList-$serverName", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("PlayerList")
        output.writeUTF(serverName)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun getServers(): CompletableFuture<List<String>> {
        val player = getFirstPlayer()
        val future = CompletableFuture<List<String>>()
        synchronized(callbackMap) {
            callbackMap.compute("GetServers", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("GetServers")
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun connect(player: Player, serverName: String) {
        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("Connect")
        output.writeUTF(serverName)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    fun connectOther(playerName: String, server: String) {
        val player = getFirstPlayer()
        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("ConnectOther")
        output.writeUTF(playerName)
        output.writeUTF(server)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    fun getIp(player: Player): CompletableFuture<InetSocketAddress> {
        val future = CompletableFuture<InetSocketAddress>()
        synchronized(callbackMap) {
            callbackMap.compute("IP", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("IP")
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun sendMessage(playerName: String, message: String) {
        val player = getFirstPlayer()
        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("Message")
        output.writeUTF(playerName)
        output.writeUTF(message)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    fun getServer(): CompletableFuture<String> {
        val player = getFirstPlayer()
        val future = CompletableFuture<String>()
        synchronized(callbackMap) {
            callbackMap.compute("GetServer", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("GetServer")
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun getUUID(player: Player): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        synchronized(callbackMap) {
            callbackMap.compute("UUID", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("UUID")
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun getUUID(playerName: String): CompletableFuture<String> {
        val player = getFirstPlayer()
        val future = CompletableFuture<String>()
        synchronized(callbackMap) {
            callbackMap.compute("UUIDOther-$playerName", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("UUIDOther")
        output.writeUTF(playerName)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun getServerIp(serverName: String): CompletableFuture<InetSocketAddress> {
        val player = getFirstPlayer()
        val future = CompletableFuture<InetSocketAddress>()
        synchronized(callbackMap) {
            callbackMap.compute("ServerIP-$serverName", computeQueueValue(future))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("ServerIP")
        output.writeUTF(serverName)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        return future
    }

    fun kickPlayer(playerName: String, kickMessage: String) {
        val player = getFirstPlayer()
        synchronized(callbackMap) {
            callbackMap.compute("KickPlayer", computeQueueValue(CompletableFuture<InetSocketAddress>()))
        }

        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("KickPlayer")
        output.writeUTF(playerName)
        output.writeUTF(kickMessage)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    fun forward(server: String, channelName: String, data: ByteArray) {
        val player = getFirstPlayer()
        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("Forward")
        output.writeUTF(server)
        output.writeUTF(channelName)
        output.writeShort(data.size)
        output.write(data)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    fun forwardToPlayer(playerName: String, channelName: String, data: ByteArray) {
        val player = getFirstPlayer()
        val output: ByteArrayDataOutput = ByteStreams.newDataOutput()
        output.writeUTF("ForwardToPlayer")
        output.writeUTF(playerName)
        output.writeUTF(channelName)
        output.writeShort(data.size)
        output.write(data)
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
    }

    private fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (!channel.equals("BungeeCord", ignoreCase = true)) return
        val input = ByteStreams.newDataInput(message)
        val subchannel = input.readUTF()

        synchronized(callbackMap) {
            var callbacks: Queue<CompletableFuture<*>>?

            when (subchannel) {
                "PlayerCount", "PlayerList", "UUIDOther", "ServerIP" -> {
                    val identifier = input.readUTF()
                    callbacks = callbackMap["$subchannel-$identifier"]
                    if (callbacks.isNullOrEmpty()) return
                    val callback = callbacks.poll()
                    try {
                        when (subchannel) {
                            "PlayerCount" -> (callback as CompletableFuture<Int>).complete(input.readInt())
                            "PlayerList" -> (callback as CompletableFuture<List<String>>)
                                .complete(input.readUTF().split(", "))
                            "UUIDOther" -> (callback as CompletableFuture<String>).complete(input.readUTF())
                            "ServerIP" -> {
                                val ip = input.readUTF()
                                val port = input.readUnsignedShort()
                                (callback as CompletableFuture<InetSocketAddress>)
                                    .complete(InetSocketAddress(ip, port))
                            }
                        }
                    } catch (ex: Exception) {
                        callback?.completeExceptionally(ex)
                    }
                    return
                }
                else -> callbacks = callbackMap[subchannel]
            }

            if (callbacks.isNullOrEmpty()) {
                val dataLength = input.readShort()
                val data = ByteArray(dataLength.toInt())
                input.readFully(data)
                globalForwardListener?.accept(subchannel, player, data)
                forwardListeners?.let { map ->
                    synchronized(map) {
                        map[subchannel]?.accept(subchannel, player, data)
                    }
                }
                return
            }

            val callback = callbacks.poll()
            try {
                when (subchannel) {
                    "GetServers" -> (callback as CompletableFuture<List<String>>)
                        .complete(input.readUTF().split(", "))
                    "GetServer", "UUID" -> (callback as CompletableFuture<String>).complete(input.readUTF())
                    "IP" -> {
                        val ip = input.readUTF()
                        val port = input.readInt()
                        (callback as CompletableFuture<InetSocketAddress>).complete(InetSocketAddress(ip, port))
                    }

                    else -> {}
                }
            } catch (ex: Exception) {
                callback?.completeExceptionally(ex)
            }
        }
    }

    fun unregister() {
        val messenger = Bukkit.getServer().messenger
        messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord", messageListener)
        messenger.unregisterOutgoingPluginChannel(plugin)
        callbackMap.clear()
    }

    private fun computeQueueValue(queueValue: CompletableFuture<*>): BiFunction<String, Queue<CompletableFuture<*>>?, Queue<CompletableFuture<*>>> {
        return BiFunction { _, value ->
            val queue = value ?: ArrayDeque()
            queue.add(queueValue)
            queue
        }
    }

    private fun getFirstPlayer(): Player {
        val firstPlayer = getFirstPlayer0(Bukkit.getOnlinePlayers())
        return firstPlayer ?: throw IllegalArgumentException("Bungee Messaging Api requires at least one player to be online.")
    }

    private fun getFirstPlayer0(playerCollection: Collection<out Player>): Player? {
        return Iterables.getFirst(playerCollection, null)
    }

    fun interface ForwardConsumer {
        fun accept(channel: String, player: Player, data: ByteArray)
    }
}
