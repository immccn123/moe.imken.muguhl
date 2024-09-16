package moe.imken.muguhl.settings

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ConfigDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "configurations.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createConfigTable = """
            CREATE TABLE ${ConfigContract.ConfigEntry.TABLE_NAME} (
                ${ConfigContract.ConfigEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ConfigContract.ConfigEntry.COLUMN_NAME} TEXT NOT NULL,
                ${ConfigContract.ConfigEntry.COLUMN_X} INTEGER NOT NULL,
                ${ConfigContract.ConfigEntry.COLUMN_Y} INTEGER NOT NULL,
                ${ConfigContract.ConfigEntry.COLUMN_WIDTH} INTEGER NOT NULL,
                ${ConfigContract.ConfigEntry.COLUMN_HEIGHT} INTEGER NOT NULL,
                ${ConfigContract.ConfigEntry.COLUMN_COLOR} INTEGER NOT NULL
            )
        """
        val createKvConfigTable = """
            CREATE TABLE ${ConfigContract.KvEntry.TABLE_NAME} (
                ${ConfigContract.KvEntry.COLUMN_KEY} TEXT UNIQUE NOT NULL,
                ${ConfigContract.KvEntry.COLUMN_VALUE} TEXT NOT NULL
            )
        """
        db.execSQL(createConfigTable)
        db.execSQL(createKvConfigTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS ${ConfigContract.ConfigEntry.TABLE_NAME}")
            db.execSQL("DROP TABLE IF EXISTS ${ConfigContract.KvEntry.TABLE_NAME}")
            onCreate(db)
        }
    }
}
