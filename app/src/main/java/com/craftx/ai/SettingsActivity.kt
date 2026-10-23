package com.craftx.ai

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class SettingsActivity:Activity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val p=getSharedPreferences("craftx",MODE_PRIVATE); val root=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER_VERTICAL;setPadding(24,24,24,24);setBackgroundColor(0xFF101318.toInt())}
 root.addView(TextView(this).apply{text="SETTINGS";textSize=26f;setTextColor(-1)})
 root.addView(TextView(this).apply{text="Demo hold duration only. It never sends clicks to games.";setTextColor(0xFFCDD5DF.toInt());setPadding(0,12,0,12)})
 val label=TextView(this).apply{setTextColor(-1);textSize=18f}; val seek=SeekBar(this).apply{max=900;progress=(p.getInt("duration",300)-100).coerceIn(0,900)}
 fun refresh(){label.text="Demo duration: ${seek.progress+100} ms"};refresh();seek.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onProgressChanged(s:SeekBar?,v:Int,f:Boolean){refresh()};override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){p.edit().putInt("duration",seek.progress+100).apply()}})
 root.addView(label);root.addView(seek);root.addView(Button(this).apply{text="SAVE";setOnClickListener{p.edit().putInt("duration",seek.progress+100).apply();finish()}});root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(root)
}}
