package com.github.ofrostdev.api.utils.common.fileconfiguration

import java.io.File
import java.io.IOException

object FileUtils {
    fun createDirectory(directory: File): Boolean {
        if (!directory.exists()) {
            return directory.mkdirs()
        }
        return true
    }

    fun createFile(file: File): Boolean {
        try {
            if (!file.exists()) {
                return file.createNewFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun listYmlFiles(directoryPath: String): Array<out File?>? {
        val directory = File("plugins" + File.separator + directoryPath)
        if (directory.exists() && directory.isDirectory) {
            return directory.listFiles { _: File?, name: String -> name.endsWith(".yml") }
        }
        return arrayOfNulls(0)
    }

    fun fileExists(path: String): Boolean {
        val file = File("plugins" + File.separator + path)
        return file.exists() && file.isFile
    }
}