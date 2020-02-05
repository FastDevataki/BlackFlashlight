package com.simplemobiletools.flashlight.activities

import android.os.Bundle
import android.view.WindowManager

class BrightDisplayActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(true)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        toggleBrightness(false)
    }

    private fun toggleBrightness(increase: Boolean) {
        val layout = window.attributes
        layout.screenBrightness = (if (increase) 1 else 0).toFloat()
        window.attributes = layout
    }
}
