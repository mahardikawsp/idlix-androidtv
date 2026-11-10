package com.idlix.data.util

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.util.Locale

object LocaleHelper {
    /**
     * Returns "id" if the device language is Indonesian ("id" or "in").
     * For English or any other language, it returns "en" as fallback.
     */
    fun getPreferredLanguage(): String {
        val locale = try {
            val config = Resources.getSystem().configuration
            val locales = ConfigurationCompat.getLocales(config)
            if (!locales.isEmpty) locales[0] else Locale.getDefault()
        } catch (_: Exception) {
            Locale.getDefault()
        }

        val lang = locale?.language?.lowercase() ?: "en"
        return if (lang == "in" || lang == "id") "id" else "en"
    }

    fun isIndonesian(): Boolean = getPreferredLanguage() == "id"
}

