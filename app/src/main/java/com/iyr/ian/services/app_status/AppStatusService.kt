package com.iyr.ian.services.app_status

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AppStatusService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PUTO", "onDestroy")
    }
}