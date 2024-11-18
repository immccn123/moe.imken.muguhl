package moe.imken.muguhl.settings

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

class ConfigManager(context: Context) {

    private val contentResolver = context.contentResolver

    // Fetch all configurations
    fun getAllConfigs(): List<Config> {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val cursor = contentResolver.query(uri, null, null, null, null)
        val configs = mutableListOf<Config>()

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_COLOR))!!

                configs.add(Config(id, name, x, y, width, height, color))
            }
        }

        return configs
    }

    // Fetch a configuration by ID
    fun getConfigById(configId: Int): Config? {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val selection = "${ConfigContract.ConfigEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(configId.toString())
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_COLOR))!!

                return Config(id, name, x, y, width, height, color)
            }
        }

        return null
    }

    // Get the currently enabled configuration
    fun getCurrentConfigId(): Int {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/kv_config")
        val selection = "${ConfigContract.KvEntry.COLUMN_KEY} = ?"
        val selectionArgs = arrayOf("current_config")
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor!!.use {
            it.moveToFirst()
            return it.getStringOrNull(it.getColumnIndex(ConfigContract.KvEntry.COLUMN_VALUE))!!
                .toInt()
        }
    }

    // Get the currently enabled configuration
    fun getCurrentConfig(): Config {
        return getConfigById(getCurrentConfigId())!!
    }

    // Utility function to get configuration by name
    fun getConfigByName(configName: String): Config? {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val selection = "${ConfigContract.ConfigEntry.COLUMN_NAME} = ?"
        val selectionArgs = arrayOf(configName)
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_ID))!!
                val name =
                    it.getStringOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_NAME))!!
                val x = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_X))!!
                val y = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_Y))!!
                val width =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_WIDTH))!!
                val height =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_HEIGHT))!!
                val color =
                    it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_COLOR))!!

                return Config(id, name, x, y, width, height, color)
            }
        }

        return null
    }

    // Create a new configuration
    fun createConfig(config: Config): Uri? {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val values = ContentValues().apply {
            put(ConfigContract.ConfigEntry.COLUMN_NAME, config.name)
            put(ConfigContract.ConfigEntry.COLUMN_X, config.x)
            put(ConfigContract.ConfigEntry.COLUMN_Y, config.y)
            put(ConfigContract.ConfigEntry.COLUMN_WIDTH, config.width)
            put(ConfigContract.ConfigEntry.COLUMN_HEIGHT, config.height)
            put(ConfigContract.ConfigEntry.COLUMN_COLOR, config.color)
        }
        return contentResolver.insert(uri, values)
    }

    // Update an existing configuration
    fun updateConfig(config: Config): Int {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val values = ContentValues().apply {
            put(ConfigContract.ConfigEntry.COLUMN_NAME, config.name)
            put(ConfigContract.ConfigEntry.COLUMN_X, config.x)
            put(ConfigContract.ConfigEntry.COLUMN_Y, config.y)
            put(ConfigContract.ConfigEntry.COLUMN_WIDTH, config.width)
            put(ConfigContract.ConfigEntry.COLUMN_HEIGHT, config.height)
            put(ConfigContract.ConfigEntry.COLUMN_COLOR, config.color)
        }
        val where = "${ConfigContract.ConfigEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(config.id.toString())
        return contentResolver.update(uri, values, where, selectionArgs)
    }

    // Remove a configuration by ID
    fun removeConfig(configId: Int): Int {
        val allConfigUri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        var firstId: Int? = null

        contentResolver.query(
            allConfigUri,
            null,
            "${ConfigContract.ConfigEntry.COLUMN_ID} != ?",
            arrayOf(configId.toString()),
            null
        ).use {
            if (it?.count == 0) {
                return 0
            }
            if (it?.moveToFirst() == true) {
                firstId = it.getIntOrNull(it.getColumnIndex(ConfigContract.ConfigEntry.COLUMN_ID))!!
            }
        }

        if (firstId !== null) switchConfig(firstId!!)

        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/configs")
        val where = "${ConfigContract.ConfigEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(configId.toString())
        return contentResolver.delete(uri, where, selectionArgs)
    }

    // Switch to a different configuration by ID (Set the current config)
    fun switchConfig(configId: Int): Int {
        val uri = Uri.parse("content://moe.imken.muguhl.settings.provider/kv_config")
        val where = "${ConfigContract.KvEntry.COLUMN_KEY} = ?"
        val selectionArgs = arrayOf("current_config")
        val values = ContentValues().apply {
            put(ConfigContract.KvEntry.COLUMN_KEY, configId.toString())
        }
        return contentResolver.update(uri, values, where, selectionArgs)
    }
}
