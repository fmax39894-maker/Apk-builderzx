package com.craftx.ai

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*

class DemoActivity:Activity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val root=LinearLayout(this).apply{orientation=1;setPadding(14,14,14,14);setBackgroundColor(0xFF101318.toInt())}
 root.addView(TextView(this).apply{text="DEMO DETECTION • APP ONLY";textSize=20f;setTextColor(-1)})
 root.addView(TextView(this).apply{text="Tap target circles to simulate detection. This does not inspect Free Fire MAX.";setTextColor(0xFFCDD5DF.toInt())})
 val status=TextView(this).apply{text="Detected targets: 0";setTextColor(-1)};root.addView(status)
 val board=Board(this){status.text="Detected targets: $it"};root.addView(board,LinearLayout.LayoutParams(-1,0,1f))
 val row=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL}
 val floating=Button(this).apply{text="●"};val lock=Button(this).apply{text="LOCK"};val unlock=Button(this).apply{text="UNLOCK"};val drag=DragListener();floating.setOnTouchListener(drag);lock.setOnClickListener{drag.locked=true;lock.text="LOCKED"};unlock.setOnClickListener{drag.locked=false;lock.text="LOCK"};row.addView(lock);row.addView(unlock);row.addView(floating,LinearLayout.LayoutParams(120,100));root.addView(row)
 root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(root)
 }
 private class DragListener:View.OnTouchListener{var locked=false;var dx=0f;var dy=0f;override fun onTouch(v:View,e:MotionEvent):Boolean{when(e.action){MotionEvent.ACTION_DOWN->{dx=v.x-e.rawX;dy=v.y-e.rawY};MotionEvent.ACTION_MOVE->{if(!locked){v.x=e.rawX+dx;v.y=e.rawY+dy}}};return true}}
 private class Board(c:android.content.Context,val report:(Int)->Unit):View(c){private val paint=Paint(3);private val targets=listOf(.25f to .30f,.70f to .48f,.45f to .72f);private var count=0
 override fun onDraw(c:Canvas){c.drawColor(0xFF252B34.toInt());targets.forEachIndexed{i,p->paint.color=if(i<count)0xFF35D07F.toInt() else 0xFFFFB84D.toInt();c.drawCircle(width*p.first,height*p.second,32f,paint);paint.color=Color.BLACK;paint.textSize=22f;c.drawText("T${i+1}",width*p.first-12,height*p.second+7,paint)};paint.color=Color.WHITE;paint.textSize=24f;c.drawText("Tap target circles",16f,32f,paint)}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_UP){val hit=targets.any{(x,y)->val dx=e.x-width*x;val dy=e.y-height*y;dx*dx+dy*dy<60*60};if(hit){count=(count+1).coerceAtMost(targets.size);report(count);invalidate()}};return true}}
}
