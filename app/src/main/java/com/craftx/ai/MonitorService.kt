package com.craftx.ai

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder

class MonitorService: Service() {
    companion object { @Volatile var running=false; const val CHANNEL="craftx_test"; const val STOP="com.craftx.ai.STOP" }
    override fun onCreate(){ super.onCreate(); if(Build.VERSION.SDK_INT>=26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"Craft X AI test controls",NotificationManager.IMPORTANCE_LOW)) }
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        if(intent?.action==STOP){ running=false; stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY }
        running=true
        val stop=PendingIntent.getService(this,1,Intent(this,MonitorService::class.java).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val b=if(Build.VERSION.SDK_INT>=26) Notification.Builder(this,CHANNEL) else Notification.Builder(this)
        val n=b.setContentTitle("Craft X AI Test is running").setContentText("Demo only — no game interaction").setSmallIcon(android.R.drawable.ic_media_play).setOngoing(true).addAction(android.R.drawable.ic_media_pause,"STOP",stop).build()
        startForeground(601,n); return START_NOT_STICKY
    }
    override fun onDestroy(){ running=false; super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
}
