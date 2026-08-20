package com.hybrid.messaging

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class MessagingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SQLCipher native libraries
        SQLiteDatabase.loadLibs(this)
    }
}
