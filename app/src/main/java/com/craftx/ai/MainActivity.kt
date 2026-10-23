package com.craftx.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var toggle: Button
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); askNotifications(); ui() }
    override fun onResume() { super.onResume(); if (::toggle.isInitialized) update() }
    private fun ui() {
        val root = LinearLayout(this).apply { orientation=1; gravity=Gravity.CENTER; setPadding(24,24,24,24); setBackgroundColor(0xFF101318.toInt()) }
        root.addView(TextView(this).apply { text="CRAFT X AI"; textSize=28f; gravity=Gravity.CENTER; setTextColor(-1) })
        status = TextView(this).apply { textSize=14f; gravity=Gravity.CENTER; setTextColor(0xFFCDD5DF.toInt()); setPadding(0,12,0,20) }
        toggle = Button(this).apply { textSize=24f; minHeight=150; setOnClickListener { if (MonitorService.running) { stopService(Intent(this@MainActivity, MonitorService::class.java)); MonitorService.running=false } else { val i=Intent(this@MainActivity, MonitorService::class.java); if(Build.VERSION.SDK_INT>=26) startForegroundService(i) else startService(i) }; update() } }
        root.addView(status, LinearLayout.LayoutParams(-1,-2)); root.addView(toggle, LinearLayout.LayoutParams(-1,170))
        root.addView(btn("DEMO DETECTION") { startActivity(Intent(this,DemoActivity::class.java)) })
        root.addView(btn("SETTINGS") { startActivity(Intent(this,SettingsActivity::class.java)) })
        root.addView(btn("MORE • LOCAL TRAINING") { startActivity(Intent(this,TrainingActivity::class.java)) })
        root.addView(btn("ALLOW FLOATING OVERLAY") { if(Build.VERSION.SDK_INT>=23 && !Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) else status.text="Overlay permission already allowed." })
        setContentView(root); update()
    }
    private fun btn(t:String, a:()->Unit)=Button(this).apply { text=t; setOnClickListener { a() } }
    private fun update(){ if(!::toggle.isInitialized)return; toggle.text=if(MonitorService.running)"STOP" else "START"; status.text=if(MonitorService.running)"Running • use notification Stop action" else "Stopped • standalone test, no backend" }
    private fun askNotifications(){ if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),22) }
}
