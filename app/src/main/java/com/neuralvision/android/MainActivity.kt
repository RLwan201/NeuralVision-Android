package com.neuralvision.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(12, 14, 22))
        }

        val title = TextView(this).apply {
            text = "NEURALVISION"
            textSize = 28f
            setTextColor(Color.rgb(0, 255, 200))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Android Graphics Lab v0.1"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 32)
        }

        val status = TextView(this).apply {
            text = "SYSTEM INITIALIZED\n\nGraphics engine: Ready\nAI engine: Experimental\nRoot access: Not required"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(status)

        setContentView(layout)
    }
}
