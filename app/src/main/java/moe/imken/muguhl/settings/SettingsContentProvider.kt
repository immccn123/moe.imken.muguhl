package moe.imken.muguhl.settings

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import moe.imken.muguhl.R

class SettingsContentProvider : ContentProvider() {

    private lateinit var dbHelper: ConfigDatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = ConfigDatabaseHelper(context!!)

        val db = dbHelper.writableDatabase
        db.query(ConfigContract.ConfigEntry.TABLE_NAME, null, null, null, null, null, null)
            .use { cursor ->
                if (!cursor.moveToFirst()) {
                    val defaultValues = ContentValues().apply {
                        put(
                            ConfigContract.ConfigEntry.COLUMN_NAME,
                            context!!.getString(R.string.default_config_name)
                        )
                        put(ConfigContract.ConfigEntry.COLUMN_X, 0)
                        put(ConfigContract.ConfigEntry.COLUMN_Y, 0)
                        put(ConfigContract.ConfigEntry.COLUMN_WIDTH, 400)
                        put(ConfigContract.ConfigEntry.COLUMN_HEIGHT, 200)
                        put(ConfigContract.ConfigEntry.COLUMN_COLOR, Color.GRAY)
                    }
                    db.insert(ConfigContract.ConfigEntry.TABLE_NAME, null, defaultValues)
                }
                cursor.close()
            }

        val addDefaultKV = { key: String, value: String ->
            db.query(
                ConfigContract.KvEntry.TABLE_NAME,
                null,
                "${ConfigContract.KvEntry.COLUMN_KEY} = ?",
                arrayOf(key),
                null,
                null,
                null
            ).use {
                if (!it.moveToFirst()) {
                    db.insert(ConfigContract.KvEntry.TABLE_NAME, null, ContentValues().apply {
                        put(ConfigContract.KvEntry.COLUMN_KEY, key)
                        put(ConfigContract.KvEntry.COLUMN_VALUE, value)
                    })
                }
                it.close()
            }
        }

        addDefaultKV("current_config", "1")
        addDefaultKV("size_locked", "0")
        addDefaultKV("pos_locked", "0")

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            CONFIGS -> db.query(
                ConfigContract.ConfigEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            KV_CONFIG -> db.query(
                ConfigContract.KvEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            CONFIGS -> {
                val id = db.insert(ConfigContract.ConfigEntry.TABLE_NAME, null, values)
                context?.contentResolver?.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id)
            }

            KV_CONFIG -> {
                db.replace(ConfigContract.KvEntry.TABLE_NAME, null, values)
                context?.contentResolver?.notifyChange(uri, null)
                uri
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            CONFIGS -> db.update(
                ConfigContract.ConfigEntry.TABLE_NAME, values, selection, selectionArgs
            )

            KV_CONFIG -> db.update(
                ConfigContract.KvEntry.TABLE_NAME, values, selection, selectionArgs
            )

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            CONFIGS -> db.delete(ConfigContract.ConfigEntry.TABLE_NAME, selection, selectionArgs)
            KV_CONFIG -> db.delete(
                ConfigContract.KvEntry.TABLE_NAME, selection, selectionArgs
            )

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            CONFIGS -> "vnd.android.cursor.dir/${ConfigContract.AUTHORITY}.${ConfigContract.ConfigEntry.TABLE_NAME}"
            KV_CONFIG -> "vnd.android.cursor.item/${ConfigContract.AUTHORITY}.${ConfigContract.KvEntry.TABLE_NAME}"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    companion object {
        const val CONFIGS = 1
        const val KV_CONFIG = 2

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(ConfigContract.AUTHORITY, "configs", CONFIGS)
            addURI(ConfigContract.AUTHORITY, "kv_config", KV_CONFIG)
        }
    }

}