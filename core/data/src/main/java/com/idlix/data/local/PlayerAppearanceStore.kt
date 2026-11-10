package com.idlix.data.local

import android.content.Context
import android.content.SharedPreferences
import com.idlix.data.model.PlayerAppearance
import com.idlix.data.model.SubtitleSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlayerAppearanceStore {

    private const val PREFS_NAME = "idlix_player_appearance"

    private const val KEY_BG_COLOR = "bg_color"
    private const val KEY_SUB_TEXT_COLOR = "sub_text_color"
    private const val KEY_SUB_BOX_COLOR = "sub_box_color"
    private const val KEY_SUB_SIZE = "sub_size"
    private const val KEY_SUB_POS_X = "sub_pos_x"
    private const val KEY_SUB_POS_Y = "sub_pos_y"
    private const val KEY_RECENT_CUSTOM_COLORS = "recent_custom_colors"

    private const val MAX_RECENT_COLORS = 6

    @Volatile
    private var prefs: SharedPreferences? = null

    private val _appearanceFlow = MutableStateFlow(PlayerAppearance())
    val appearance: StateFlow<PlayerAppearance> = _appearanceFlow.asStateFlow()

    private val _recentColorsFlow = MutableStateFlow<List<Int>>(emptyList())
    val recentCustomColors: StateFlow<List<Int>> = _recentColorsFlow.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            synchronized(this) {
                if (prefs == null) {
                    val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs = p
                    _appearanceFlow.value = loadAppearance(p)
                    _recentColorsFlow.value = loadRecentColors(p)
                }
            }
        }
    }

    private fun loadAppearance(p: SharedPreferences): PlayerAppearance {
        val bgColor = p.getInt(KEY_BG_COLOR, PlayerAppearance.DEFAULT_BG_COLOR)
        val subTextColor = p.getInt(KEY_SUB_TEXT_COLOR, PlayerAppearance.DEFAULT_SUB_TEXT_COLOR)
        val subBoxColor = p.getInt(KEY_SUB_BOX_COLOR, PlayerAppearance.DEFAULT_SUB_BOX_COLOR)

        val sizeName = p.getString(KEY_SUB_SIZE, SubtitleSize.MEDIUM.name) ?: SubtitleSize.MEDIUM.name
        val subSize = try {
            SubtitleSize.valueOf(sizeName)
        } catch (_: Exception) {
            SubtitleSize.MEDIUM
        }

        val rawX = p.getFloat(KEY_SUB_POS_X, PlayerAppearance.DEFAULT_POS_X)
        val rawY = p.getFloat(KEY_SUB_POS_Y, PlayerAppearance.DEFAULT_POS_Y)

        val posX = rawX.coerceIn(PlayerAppearance.SAFE_X_RANGE)
        val posY = rawY.coerceIn(PlayerAppearance.SAFE_Y_RANGE)

        return PlayerAppearance(
            backgroundColor = bgColor,
            subtitleTextColor = subTextColor,
            subtitleBoxColor = subBoxColor,
            subtitleSize = subSize,
            subtitlePosX = posX,
            subtitlePosY = posY
        )
    }

    private fun loadRecentColors(p: SharedPreferences): List<Int> {
        val raw = p.getString(KEY_RECENT_CUSTOM_COLORS, null) ?: return emptyList()
        return raw.split(",")
            .mapNotNull { it.trim().toLongOrNull(16)?.toInt() }
            .take(MAX_RECENT_COLORS)
    }

    fun update(transform: (PlayerAppearance) -> PlayerAppearance) {
        val current = _appearanceFlow.value
        val updated = transform(current)

        val clamped = updated.copy(
            subtitlePosX = updated.subtitlePosX.coerceIn(PlayerAppearance.SAFE_X_RANGE),
            subtitlePosY = updated.subtitlePosY.coerceIn(PlayerAppearance.SAFE_Y_RANGE)
        )

        _appearanceFlow.value = clamped

        prefs?.edit()?.apply {
            putInt(KEY_BG_COLOR, clamped.backgroundColor)
            putInt(KEY_SUB_TEXT_COLOR, clamped.subtitleTextColor)
            putInt(KEY_SUB_BOX_COLOR, clamped.subtitleBoxColor)
            putString(KEY_SUB_SIZE, clamped.subtitleSize.name)
            putFloat(KEY_SUB_POS_X, clamped.subtitlePosX)
            putFloat(KEY_SUB_POS_Y, clamped.subtitlePosY)
            apply()
        }
    }

    fun addRecentCustomColor(color: Int) {
        val currentList = _recentColorsFlow.value.toMutableList()
        currentList.remove(color)
        currentList.add(0, color)
        val trimmed = currentList.take(MAX_RECENT_COLORS)
        _recentColorsFlow.value = trimmed

        val raw = trimmed.joinToString(",") { String.format("%08X", it) }
        prefs?.edit()?.putString(KEY_RECENT_CUSTOM_COLORS, raw)?.apply()
    }

    fun resetPosition() {
        update {
            it.copy(
                subtitlePosX = PlayerAppearance.DEFAULT_POS_X,
                subtitlePosY = PlayerAppearance.DEFAULT_POS_Y
            )
        }
    }

    fun resetAll() {
        val defaultAppearance = PlayerAppearance()
        _appearanceFlow.value = defaultAppearance

        prefs?.edit()?.apply {
            putInt(KEY_BG_COLOR, defaultAppearance.backgroundColor)
            putInt(KEY_SUB_TEXT_COLOR, defaultAppearance.subtitleTextColor)
            putInt(KEY_SUB_BOX_COLOR, defaultAppearance.subtitleBoxColor)
            putString(KEY_SUB_SIZE, defaultAppearance.subtitleSize.name)
            putFloat(KEY_SUB_POS_X, defaultAppearance.subtitlePosX)
            putFloat(KEY_SUB_POS_Y, defaultAppearance.subtitlePosY)
            apply()
        }
    }
}

