package com.github.ofrostdev.api;

import com.github.ofrostdev.api.task.TaskController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@AllArgsConstructor
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String repoUser;
    private final String repoName;

    public void check(File file){
        new TaskController(false, TaskController.Type.RUN_ONCE, 0L, 0L) {
            @Override
            public void handle() {
                checkAndDownload(file);
            }
        }.start();
    }

    private void checkAndDownload(File file) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + repoUser + "/" + repoName + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Authorization", "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");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();

                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(response.toString()).getAsJsonObject();
                String latestTag = json.get("tag_name").getAsString();
                String currentVersion = plugin.getDescription().getVersion();

                String latest = normalize(latestTag);
                String current = normalize(currentVersion);

                if (!current.equals(latest)) {
                    plugin.getLogger().warning("Nova versão disponível: " + latestTag + " (Atual: " + currentVersion + ")");

                    String downloadUrl = json.getAsJsonArray("assets")
                            .get(0).getAsJsonObject().get("browser_download_url").getAsString();

                    File tempFile = new File(plugin.getDataFolder().getParentFile(),
                            file.getName().replace(".jar", "-update.jar"));

                    downloadFile(downloadUrl, tempFile);
                    plugin.getLogger().info("Nova versão baixada em: " + tempFile.getAbsolutePath());

                    String msg = ChatColor.RED + "Servidor será desligado em 5 segundos para você efetuar a atualização.";
                    plugin.getLogger().warning(msg);

                    new TaskController(false, TaskController.Type.RUN_ONCE, 20L * 5, 0) {
                        @Override
                        public void handle() {
                            Bukkit.shutdown();
                        }
                    }.start();
                } else {
                    plugin.getLogger().info("Você está usando a versão mais recente!");
                }

            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao verificar/baixar atualização: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void downloadFile(String downloadUrl, File destination) throws IOException {
        HttpURLConnection dl = (HttpURLConnection) new URL(downloadUrl).openConnection();
        dl.setRequestProperty("User-Agent", "Mozilla/5.0");
        dl.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream input = dl.getInputStream();
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    private String normalize(String version) {
        return version.toLowerCase().replaceAll("[^0-9\\.]", "");
    }
}
