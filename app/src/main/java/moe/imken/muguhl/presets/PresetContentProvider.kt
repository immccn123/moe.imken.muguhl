package moe.imken.muguhl.presets

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import moe.imken.muguhl.R

class PresetContentProvider : ContentProvider() {

    private lateinit var dbHelper: PresetDatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = PresetDatabaseHelper(context!!)

        val db = dbHelper.writableDatabase
        db.query(PresetContract.PresetEntry.TABLE_NAME, null, null, null, null, null, null)
            .use { cursor ->
                if (!cursor.moveToFirst()) {
                    val defaultValues = ContentValues().apply {
                        put(
                            PresetContract.PresetEntry.COLUMN_NAME,
                            context!!.getString(R.string.default_preset_name)
                        )
                        put(PresetContract.PresetEntry.COLUMN_X, 0)
                        put(PresetContract.PresetEntry.COLUMN_Y, 0)
                        put(PresetContract.PresetEntry.COLUMN_WIDTH, 400)
                        put(PresetContract.PresetEntry.COLUMN_HEIGHT, 200)
                        put(PresetContract.PresetEntry.COLUMN_COLOR, Color.GRAY)
                    }
                    db.insert(PresetContract.PresetEntry.TABLE_NAME, null, defaultValues)
                }
                cursor.close()
            }

        val addDefaultKV = { key: String, value: String ->
            db.query(
                PresetContract.KvEntry.TABLE_NAME,
                null,
                "${PresetContract.KvEntry.COLUMN_KEY} = ?",
                arrayOf(key),
                null,
                null,
                null
            ).use {
                if (!it.moveToFirst()) {
                    db.insert(PresetContract.KvEntry.TABLE_NAME, null, ContentValues().apply {
                        put(PresetContract.KvEntry.COLUMN_KEY, key)
                        put(PresetContract.KvEntry.COLUMN_VALUE, value)
                    })
                }
                it.close()
            }
        }

        addDefaultKV(SettingsItem.CURRENT_PRESET, "1")
        addDefaultKV(SettingsItem.SIZE_LOCKED, "0")
        addDefaultKV(SettingsItem.POS_LOCKED, "0")

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
            PRESETS -> db.query(
                PresetContract.PresetEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            KV_CONFIG -> db.query(
                PresetContract.KvEntry.TABLE_NAME,
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
            PRESETS -> {
                val id = db.insert(PresetContract.PresetEntry.TABLE_NAME, null, values)
                context?.contentResolver?.notifyChange(uri, null)
                ContentUris.withAppendedId(uri, id)
            }

            KV_CONFIG -> {
                db.replace(PresetContract.KvEntry.TABLE_NAME, null, values)
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
            PRESETS -> db.update(
                PresetContract.PresetEntry.TABLE_NAME, values, selection, selectionArgs
            )

            KV_CONFIG -> db.update(
                PresetContract.KvEntry.TABLE_NAME, values, selection, selectionArgs
            )

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            PRESETS -> db.delete(PresetContract.PresetEntry.TABLE_NAME, selection, selectionArgs)
            KV_CONFIG -> db.delete(
                PresetContract.KvEntry.TABLE_NAME, selection, selectionArgs
            )

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            PRESETS -> "vnd.android.cursor.dir/${PresetContract.AUTHORITY}.${PresetContract.PresetEntry.TABLE_NAME}"
            KV_CONFIG -> "vnd.android.cursor.item/${PresetContract.AUTHORITY}.${PresetContract.KvEntry.TABLE_NAME}"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    companion object {
        const val PRESETS = 1
        const val KV_CONFIG = 2

        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PresetContract.AUTHORITY, "presets", PRESETS)
            addURI(PresetContract.AUTHORITY, "kv_config", KV_CONFIG)
        }
    }
}