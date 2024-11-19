package moe.imken.muguhl.presets

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PresetDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "configurations.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createConfigTable = """
            CREATE TABLE ${PresetContract.PresetEntry.TABLE_NAME} (
                ${PresetContract.PresetEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${PresetContract.PresetEntry.COLUMN_NAME} TEXT NOT NULL,
                ${PresetContract.PresetEntry.COLUMN_X} INTEGER NOT NULL,
                ${PresetContract.PresetEntry.COLUMN_Y} INTEGER NOT NULL,
                ${PresetContract.PresetEntry.COLUMN_WIDTH} INTEGER NOT NULL,
                ${PresetContract.PresetEntry.COLUMN_HEIGHT} INTEGER NOT NULL,
                ${PresetContract.PresetEntry.COLUMN_COLOR} INTEGER NOT NULL
            )
        """
        val createKvConfigTable = """
            CREATE TABLE ${PresetContract.KvEntry.TABLE_NAME} (
                ${PresetContract.KvEntry.COLUMN_KEY} TEXT UNIQUE NOT NULL,
                ${PresetContract.KvEntry.COLUMN_VALUE} TEXT NOT NULL
            )
        """
        db.execSQL(createConfigTable)
        db.execSQL(createKvConfigTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS ${PresetContract.PresetEntry.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${PresetContract.KvEntry.TABLE_NAME}")
            onCreate(db)
        }
    }
}
