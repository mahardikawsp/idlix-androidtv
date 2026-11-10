package com.idlix.data.update

enum class AppPlatform(val sectionName: String) {
    TV("TV"),
    MOBILE("Mobile")
}

data class ReleaseSectionInfo(
    val version: String,
    val link: String
)

object ReleaseIniParser {

    fun parse(iniContent: String, platform: AppPlatform): ReleaseSectionInfo? {
        val sanitized = iniContent.removePrefix("\uFEFF")
        var currentSection: String? = null
        var version: String? = null
        var link: String? = null

        for (rawLine in sanitized.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                continue
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length - 1).trim()
                continue
            }

            if (currentSection != null && currentSection.equals(platform.sectionName, ignoreCase = true)) {
                val eqIndex = line.indexOf('=')
                if (eqIndex > 0) {
                    val key = line.substring(0, eqIndex).trim()
                    val value = line.substring(eqIndex + 1).trim()
                        .removeSurrounding("\"")
                        .removeSurrounding("'")
                        .trim()

                    when {
                        key.equals("Version", ignoreCase = true) -> version = value
                        key.equals("Link", ignoreCase = true) -> link = value
                    }
                }
            }
        }

        val resolvedVersion = version?.takeIf { it.isNotBlank() } ?: return null
        val resolvedLink = link?.takeIf { it.isNotBlank() } ?: return null
        return ReleaseSectionInfo(version = resolvedVersion, link = resolvedLink)
    }

    fun normalizeVersion(version: String): String {
        return version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .trim()
    }

    /**
     * Membandingkan dua string versi secara semantik per segmen titik.
     * Mengembalikan nilai < 0 jika currentVersion < remoteVersion,
     * 0 jika setara secara numerik, atau > 0 jika currentVersion > remoteVersion.
     */
    fun compareSemanticVersion(currentVersion: String, remoteVersion: String): Int {
        val currentParts = normalizeVersion(currentVersion).split(".")
        val remoteParts = normalizeVersion(remoteVersion).split(".")
        val maxLen = maxOf(currentParts.size, remoteParts.size)

        for (i in 0 until maxLen) {
            val curStr = currentParts.getOrNull(i)?.filter { it.isDigit() }.orEmpty()
            val remStr = remoteParts.getOrNull(i)?.filter { it.isDigit() }.orEmpty()

            val curNum = curStr.toIntOrNull() ?: 0
            val remNum = remStr.toIntOrNull() ?: 0

            if (curNum != remNum) {
                return curNum.compareTo(remNum)
            }
        }
        return 0
    }

    /**
     * Mengembalikan true apabila versi aplikasi saat ini tidak cocok atau berada di bawah
     * key Version pada endpoint releases.ini.
     */
    fun isUpdateRequired(currentVersion: String, remoteVersion: String): Boolean {
        val cleanCurrent = normalizeVersion(currentVersion)
        val cleanRemote = normalizeVersion(remoteVersion)
        if (cleanRemote.isEmpty()) return false
        if (cleanCurrent.equals(cleanRemote, ignoreCase = true)) return false
        return compareSemanticVersion(cleanCurrent, cleanRemote) < 0 || !cleanCurrent.equals(cleanRemote, ignoreCase = true)
    }
}

