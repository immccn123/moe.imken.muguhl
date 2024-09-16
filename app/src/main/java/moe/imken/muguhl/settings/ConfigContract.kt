package moe.imken.muguhl.settings

object ConfigContract {
    const val AUTHORITY = "moe.imken.muguhl.settings.provider"

    object ConfigEntry {
        const val TABLE_NAME = "config"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_X = "x"
        const val COLUMN_Y = "y"
        const val COLUMN_WIDTH = "width"
        const val COLUMN_HEIGHT = "height"
        const val COLUMN_COLOR = "color"
    }

    object KvEntry {
        const val TABLE_NAME = "settings"
        const val COLUMN_KEY = "setting_key"
        const val COLUMN_VALUE = "value"
    }
}
