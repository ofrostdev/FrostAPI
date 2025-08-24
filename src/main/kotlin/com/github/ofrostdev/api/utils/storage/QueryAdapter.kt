package com.github.ofrostdev.api.utils.storage

enum class DatabaseType {
    MYSQL, SQLITE
}

object QueryAdapter {

    private val mysqlToSqliteRules = listOf(
        "ON DUPLICATE KEY UPDATE" to "INSERT OR REPLACE INTO",
        "INT AUTO_INCREMENT" to "INTEGER PRIMARY KEY AUTOINCREMENT",
        "BIGINT" to "INTEGER",
        "DATETIME" to "TEXT",
        "BOOLEAN" to "INTEGER",
        "DECIMAL" to "REAL",
        "DOUBLE" to "REAL",
        "FLOAT" to "REAL",
        "TINYINT(1)" to "INTEGER",
        "NOW()" to "CURRENT_TIMESTAMP",
        "CURRENT_TIMESTAMP()" to "CURRENT_TIMESTAMP",
        "ENGINE=InnoDB" to "",
        "UNSIGNED" to "",
        "ON UPDATE CURRENT_TIMESTAMP" to ""
    )

    private val sqliteToMysqlRules = listOf(
        "INSERT OR REPLACE INTO" to "INSERT INTO",
        "INTEGER PRIMARY KEY AUTOINCREMENT" to "INT AUTO_INCREMENT PRIMARY KEY",
        "REAL" to "DECIMAL(18, 8)",
        "CURRENT_TIMESTAMP" to "NOW()"
    )

    fun adapt(query: String, type: DatabaseType): String {
        return when (type) {
            DatabaseType.MYSQL -> adaptToMySQL(query)
            DatabaseType.SQLITE -> adaptToSQLite(query)
        }
    }

    private fun adaptToSQLite(query: String): String {
        var q = query
        val isUpsert = q.contains("ON DUPLICATE KEY UPDATE", ignoreCase = true)

        mysqlToSqliteRules.forEach { (mysql, sqlite) ->
            if (mysql == "ON DUPLICATE KEY UPDATE") {
                if (isUpsert) {
                    val updateClauseRegex = Regex("\\sON DUPLICATE KEY UPDATE.*", RegexOption.IGNORE_CASE)
                    q = q.replace(updateClauseRegex, "").trim()
                    q = q.replaceFirst("INSERT INTO", "INSERT OR REPLACE INTO", ignoreCase = true)
                }
            } else {
                q = q.replace(mysql, sqlite, ignoreCase = true)
            }
        }

        q = q.replace(Regex("VARCHAR\\(\\d+\\)", RegexOption.IGNORE_CASE), "TEXT")
        q = q.replace(Regex("LIMIT\\s+\\?,\\s+\\?", RegexOption.IGNORE_CASE), "LIMIT ? OFFSET ?")

        return q
    }

    private fun adaptToMySQL(query: String): String {
        var q = query
        val isUpsert = q.contains("INSERT OR REPLACE INTO", ignoreCase = true)

        sqliteToMysqlRules.forEach { (sqlite, mysql) ->
            if (sqlite == "INSERT OR REPLACE INTO") {
                if (isUpsert) {
                    q = q.replaceFirst(sqlite, mysql, ignoreCase = true)
                    val columns = q.substringAfter('(').substringBefore(')').split(',').map { it.trim() }
                    val updateSet = columns.joinToString(", ") { "$it = VALUES($it)" }
                    q += " ON DUPLICATE KEY UPDATE $updateSet"
                }
            } else {
                q = q.replace(sqlite, mysql, ignoreCase = true)
            }
        }

        q = q.replace(Regex("LIMIT\\s+\\?\\s+OFFSET\\s+\\?", RegexOption.IGNORE_CASE), "LIMIT ?, ?")
        q = q.replace("TEXT", "TEXT", ignoreCase = true)

        return q
    }
}
