package com.craftx.ai

import android.app.Activity
import android.os.Bundle
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class TrainingActivity:Activity(){private lateinit var note:EditText;private lateinit var label:EditText;private lateinit var saved:TextView
 private val p by lazy{getSharedPreferences("craftx",MODE_PRIVATE)}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val r=LinearLayout(this).apply{orientation=1;setPadding(22,22,22,22);setBackgroundColor(0xFF101318.toInt())}
 r.addView(TextView(this).apply{text="LOCAL TRAINING";textSize=25f;setTextColor(-1)})
 r.addView(TextView(this).apply{text="Save labeled notes locally. This does not train an ML model or inspect other apps.";setTextColor(0xFFCDD5DF.toInt())})
 note=EditText(this).apply{hint="Example note"};label=EditText(this).apply{hint="Label: target / background / unsure"};r.addView(note);r.addView(label)
 r.addView(Button(this).apply{text="SAVE EXAMPLE";setOnClickListener{save()}});r.addView(Button(this).apply{text="OPEN DEMO DETECTION";setOnClickListener{startActivity(android.content.Intent(this@TrainingActivity,DemoActivity::class.java))}})
 saved=TextView(this).apply{setTextColor(-1);setPadding(0,12,0,12)};r.addView(saved);r.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(r);refresh() }
 private fun save(){if(note.text.isBlank()||label.text.isBlank()){note.error="Enter note and label";return};val a=try{JSONArray(p.getString("examples","[]"))}catch(e:Exception){JSONArray()};a.put(JSONObject().put("note",note.text.toString()).put("label",label.text.toString()).put("time",System.currentTimeMillis()));p.edit().putString("examples",a.toString()).apply();note.text.clear();label.text.clear();refresh()}
 private fun refresh(){val a=try{JSONArray(p.getString("examples","[]"))}catch(e:Exception){JSONArray()};saved.text="Saved examples: ${a.length()}\n"+(0 until a.length()).takeLast(12).joinToString("\n"){i->val o=a.getJSONObject(i);"${i+1}. ${o.optString("label")}: ${o.optString("note")}"}.ifEmpty{"No examples yet."}}
}
