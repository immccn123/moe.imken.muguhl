package moe.imken.muguhl

import android.app.Application
import com.google.android.material.color.DynamicColors

class MuguhlApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}