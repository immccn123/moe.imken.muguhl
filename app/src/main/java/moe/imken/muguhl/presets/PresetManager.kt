package moe.imken.muguhl.presets

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

class PresetManager(context: Context) {

    private val contentResolver = context.contentResolver

    fun getAllPresets(): List<Preset> {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val cursor = contentResolver.query(uri, null, null, null, null)
        val presets = mutableListOf<Preset>()

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_COLOR))!!

                presets.add(Preset(id, name, x, y, width, height, color))
            }
        }

        return presets
    }

    fun getPresetById(presetId: Int): Preset? {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val selection = "${PresetContract.PresetEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(presetId.toString())
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_COLOR))!!

                return Preset(id, name, x, y, width, height, color)
            }
        }

        return null
    }

    fun getCurrentPresetId(): Int {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/kv_config")
        val selection = "${PresetContract.KvEntry.COLUMN_KEY} = ?"
        val selectionArgs = arrayOf(SettingsItem.CURRENT_PRESET)
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor!!.use {
            it.moveToFirst()
            return it.getStringOrNull(it.getColumnIndex(PresetContract.KvEntry.COLUMN_VALUE))!!
                .toInt()
        }
    }
    
    fun getCurrentPreset(): Preset {
        return getPresetById(getCurrentPresetId())!!
    }
    
    fun getPresetByName(presetName: String): Preset? {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val selection = "${PresetContract.PresetEntry.COLUMN_NAME} = ?"
        val selectionArgs = arrayOf(presetName)
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_COLOR))!!

                return Preset(id, name, x, y, width, height, color)
            }
        }

        return null
    }

    fun createPreset(preset: Preset): Uri? {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val values = ContentValues().apply {
            put(PresetContract.PresetEntry.COLUMN_NAME, preset.name)
            put(PresetContract.PresetEntry.COLUMN_X, preset.x)
            put(PresetContract.PresetEntry.COLUMN_Y, preset.y)
            put(PresetContract.PresetEntry.COLUMN_WIDTH, preset.width)
            put(PresetContract.PresetEntry.COLUMN_HEIGHT, preset.height)
            put(PresetContract.PresetEntry.COLUMN_COLOR, preset.color)
        }
        return contentResolver.insert(uri, values)
    }
    
    fun updatePreset(preset: Preset): Int {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val values = ContentValues().apply {
            put(PresetContract.PresetEntry.COLUMN_NAME, preset.name)
            put(PresetContract.PresetEntry.COLUMN_X, preset.x)
            put(PresetContract.PresetEntry.COLUMN_Y, preset.y)
            put(PresetContract.PresetEntry.COLUMN_WIDTH, preset.width)
            put(PresetContract.PresetEntry.COLUMN_HEIGHT, preset.height)
            put(PresetContract.PresetEntry.COLUMN_COLOR, preset.color)
        }
        val where = "${PresetContract.PresetEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(preset.id.toString())
        return contentResolver.update(uri, values, where, selectionArgs)
    }

    fun removePreset(presetId: Int): Int {
        val allPresetUri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        var firstId: Int? = null

        contentResolver.query(
            allPresetUri,
            null,
            "${PresetContract.PresetEntry.COLUMN_ID} != ?",
            arrayOf(presetId.toString()),
            null
        ).use {
            if (it?.count == 0) {
                return 0
            }
            if (it?.moveToFirst() == true) {
                firstId = it.getIntOrNull(it.getColumnIndex(PresetContract.PresetEntry.COLUMN_ID))!!
            }
        }

        if (presetId == getCurrentPresetId() && firstId !== null) switchPreset(firstId!!)

        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/presets")
        val where = "${PresetContract.PresetEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(presetId.toString())
        return contentResolver.delete(uri, where, selectionArgs)
    }

    fun switchPreset(presetId: Int): Int {
        val uri = Uri.parse("content://${PresetContract.AUTHORITY}/kv_config")
        val where = "${PresetContract.KvEntry.COLUMN_KEY} = ?"
        val selectionArgs = arrayOf(SettingsItem.CURRENT_PRESET)
        val values = ContentValues().apply {
            put(PresetContract.KvEntry.COLUMN_VALUE, presetId.toString())
        }
        return contentResolver.update(uri, values, where, selectionArgs)
    }
}
