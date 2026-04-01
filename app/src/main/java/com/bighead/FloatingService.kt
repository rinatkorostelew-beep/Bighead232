package com.bighead

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.SeekBar
import android.widget.TextView

class FloatingService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var root: View
    private lateinit var lp: WindowManager.LayoutParams
    private var ix = 0; private var iy = 0
    private var tx = 0f; private var ty = 0f

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        root = LayoutInflater.from(this).inflate(R.layout.layout_overlay, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.START
        lp.x = 40
        lp.y = 200

        wm.addView(root, lp)

        val seekBar = root.findViewById<SeekBar>(R.id.seekHead)
        val tvVal = root.findViewById<TextView>(R.id.tvValue)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, f: Boolean) {
                tvVal.text = "${p + 50}%"
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })

        root.findViewById<View>(R.id.btnClose).setOnClickListener { stopSelf() }

        root.findViewById<View>(R.id.dragHandle).setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { ix = lp.x; iy = lp.y; tx = e.rawX; ty = e.rawY; true }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = ix + (e.rawX - tx).toInt()
                    lp.y = iy + (e.rawY - ty).toInt()
                    wm.updateViewLayout(root, lp); true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::root.isInitialized) wm.removeView(root)
    }
}
