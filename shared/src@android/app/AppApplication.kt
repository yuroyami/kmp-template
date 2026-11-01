package app

import android.app.Application
import android.content.Context

internal lateinit var appContext: Context
    private set

/** The Android process start. The manifest names this class. */
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        startApp()
    }
}
