package com.simplemobiletools.flashlight.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.flashlight.BuildConfig
import com.simplemobiletools.flashlight.R
import com.simplemobiletools.flashlight.extensions.config
import com.simplemobiletools.flashlight.helpers.BusProvider
import com.simplemobiletools.flashlight.helpers.MyCameraImpl
import com.squareup.otto.Bus
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity() {

    private val FLASHLIGHT_STATE = "flashlight_state"
    private var mBus: Bus? = null
    private var mCameraImpl: MyCameraImpl? = null
    private var mIsFlashlightOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        mBus = BusProvider.instance

        flashlight_btn.setOnClickListener {
            mCameraImpl!!.toggleFlashlight()
        }
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        mCameraImpl!!.handleCameraSetup()
        checkState(MyCameraImpl.isFlashlightOn)
        updateTextColors(main_holder)
        invalidateOptionsMenu()
    }

    override fun onStart() {
        super.onStart()
        mBus!!.register(this)

        if (mCameraImpl == null) {
            setupCameraImpl()
        }
    }

    override fun onStop() {
        super.onStop()
        mBus!!.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(FLASHLIGHT_STATE, mIsFlashlightOn)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val isFlashlightOn = savedInstanceState.getBoolean(FLASHLIGHT_STATE, false)
        if (isFlashlightOn) {
            mCameraImpl!!.toggleFlashlight()
        }
    }

    private fun setupCameraImpl() {
        mCameraImpl = MyCameraImpl.newInstance(this)
        if (config.turnFlashlightOn) {
            mCameraImpl!!.enableFlashlight()
        }
    }

    private fun setupStroboscope() {

    }

    private fun getContrastColor() = config.backgroundColor.getContrastColor()

    private fun releaseCamera() {
        mCameraImpl?.releaseCamera()
        mCameraImpl = null
    }


    private fun checkState(isEnabled: Boolean) {
        if (isEnabled) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    private fun enableFlashlight() {
        changeIconColor(getAdjustedPrimaryColor(), flashlight_btn)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = true
    }

    private fun disableFlashlight() {
        changeIconColor(getContrastColor(), flashlight_btn)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mIsFlashlightOn = false
    }

    private fun changeIconColor(color: Int, imageView: ImageView?) {
        imageView!!.background.mutate().applyColorFilter(color)
    }

}
