package com.github.ofrostdev.api.utils.storage

enum class DatabaseType {
    MYSQL, SQLITE
}

object QueryAdapter {

    fun adapt(query: String, type: DatabaseType): String {
        return when (type) {
            DatabaseType.MYSQL -> adaptToMySQL(query)
            DatabaseType.SQLITE -> adaptToSQLite(query)
        }
    }

    private fun adaptToSQLite(query: String): String {
        var q = query

        q = q.replace("INT AUTO_INCREMENT", "INTEGER PRIMARY KEY AUTOINCREMENT", ignoreCase = true)
        q = q.replace("BIGINT", "INTEGER", ignoreCase = true)
        q = q.replace("DECIMAL", "REAL", ignoreCase = true)
        q = q.replace("FLOAT", "REAL", ignoreCase = true)
        q = q.replace("BOOLEAN", "INTEGER", ignoreCase = true)
        q = q.replace("DATETIME", "TEXT", ignoreCase = true)
        q = q.replace(Regex("VARCHAR\\(\\d+\\)", RegexOption.IGNORE_CASE), "TEXT")

        q = q.replace("ON UPDATE CURRENT_TIMESTAMP", "", ignoreCase = true)
        q = q.replace("ENGINE=InnoDB", "", ignoreCase = true)
        q = q.replace("UNSIGNED", "", ignoreCase = true)

        return q
    }

    private fun adaptToMySQL(query: String): String {
        var q = query

        q = q.replace("INTEGER PRIMARY KEY AUTOINCREMENT", "INT AUTO_INCREMENT PRIMARY KEY", ignoreCase = true)
        q = q.replace("REAL", "DECIMAL(15,5)", ignoreCase = true)
        q = q.replace("BOOLEAN", "TINYINT(1)", ignoreCase = true)
        q = q.replace("TEXT", "VARCHAR(65535)", ignoreCase = true)

        return q
    }
}
