package com.github.ofrostdev.api

import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class UpdateChecker(private val plugin: JavaPlugin, private val repoUser: String, private val repoName: String) {
    fun check(file: File) {
        Bukkit.getScheduler().runTask(plugin) { checkAndDownload(file) }
    }

    private fun checkAndDownload(file: File) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            try {
                val url = URL("https://api.github.com/repos/$repoUser/$repoName/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                conn.setRequestProperty("Authorization", "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")

                val `in` = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while ((`in`.readLine().also { line = it }) != null) response.append(line)
                `in`.close()

                val parser = JsonParser()
                val json = parser.parse(response.toString()).asJsonObject
                val latestTag = json["tag_name"].asString
                val currentVersion = plugin.description.version

                val latest = normalize(latestTag)
                val current = normalize(currentVersion)

                if (current != latest) {
                    plugin.logger.warning("Nova versão disponível: $latestTag (Atual: $currentVersion)")

                    val downloadUrl = json.getAsJsonArray("assets")[0].asJsonObject["browser_download_url"].asString

                    val tempFile = File(
                        plugin.dataFolder.parentFile,
                        file.name.replace(".jar", "-update.jar")
                    )

                    downloadFile(downloadUrl, tempFile)
                    plugin.logger.info("Nova versão baixada em: " + tempFile.absolutePath)

                    val msg =
                        ChatColor.RED.toString() + "Servidor será desligado em 5 segundos para você efetuar a atualização."
                    plugin.logger.warning(msg)

                    Bukkit.getScheduler().runTaskLater(plugin, { Bukkit.shutdown() }, (20 * 5).toLong())
                } else {
                    plugin.logger.info("Você está usando a versão mais recente!")
                }
            } catch (e: Exception) {
                plugin.logger.severe("Erro ao verificar/baixar atualização: " + e.message)
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun downloadFile(downloadUrl: String, destination: File) {
        val dl = URL(downloadUrl).openConnection() as HttpURLConnection
        dl.setRequestProperty("User-Agent", "Mozilla/5.0")

        dl.inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while ((input.read(buffer).also { bytesRead = it }) != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun normalize(version: String): String {
        return version.lowercase(Locale.getDefault()).replace("[^0-9\\.]".toRegex(), "")
    }
}