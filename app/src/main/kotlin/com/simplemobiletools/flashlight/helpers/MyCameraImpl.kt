package com.simplemobiletools.flashlight.helpers

import android.content.Context
import android.hardware.Camera
import android.os.Handler
import com.simplemobiletools.commons.helpers.isMarshmallowPlus
import com.simplemobiletools.flashlight.models.Events
import com.squareup.otto.Bus


class MyCameraImpl(val context: Context) {
    companion object {
        var isFlashlightOn = false
        private var camera: Camera? = null
        private var params: Camera.Parameters? = null
        private var bus: Bus? = null
        private var isMarshmallow = false
        private var shouldEnableFlashlight = false
        private var marshmallowCamera: MarshmallowCamera? = null
        @Volatile
        private var shouldStroboscopeStop = false
        @Volatile
        private var isStroboscopeRunning = false
        @Volatile
        private var isSOSRunning = false
        fun newInstance(context: Context) = MyCameraImpl(context)
    }

    init {
        isMarshmallow = isMarshmallowPlus()

        if (bus == null) {
            bus = BusProvider.instance
            bus!!.register(this)
        }

        handleCameraSetup()
    }

    fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        checkFlashlight()
    }


    fun handleCameraSetup() {
        if (isMarshmallow) {
            setupMarshmallowCamera()
        } else {
            setupCamera()
        }
    }

    private fun setupMarshmallowCamera() {
        if (marshmallowCamera == null) {
            marshmallowCamera = MarshmallowCamera(context)
        }
    }

    private fun setupCamera() {
        if (isMarshmallow) {
            return
        }

        if (camera == null) {
            initCamera()
        }
    }

    private fun initCamera() {
        try {
            camera = Camera.open()
            params = camera!!.parameters
            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        } catch (e: Exception) {
            bus!!.post(Events.CameraUnavailable())
        }
    }

    private fun checkFlashlight() {
        if (camera == null) {
            handleCameraSetup()
        }

        if (isFlashlightOn) {
            enableFlashlight()
        } else {
            disableFlashlight()
        }
    }

    fun enableFlashlight() {
        shouldStroboscopeStop = true
        if (isStroboscopeRunning || isSOSRunning) {
            shouldEnableFlashlight = true
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(true)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = params
            camera!!.startPreview()
        }

        val mainRunnable = Runnable { stateChanged(true) }
        Handler(context.mainLooper).post(mainRunnable)
    }

    private fun disableFlashlight() {
        if (isStroboscopeRunning || isSOSRunning) {
            return
        }

        if (isMarshmallow) {
            toggleMarshmallowFlashlight(false)
        } else {
            if (camera == null || params == null) {
                return
            }

            params!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = params
        }
        stateChanged(false)
        releaseCamera()
    }

    private fun stateChanged(isEnabled: Boolean) {
        isFlashlightOn = isEnabled
        bus!!.post(Events.StateChanged(isEnabled))

    }

    private fun toggleMarshmallowFlashlight(enable: Boolean) {
        marshmallowCamera!!.toggleMarshmallowFlashlight(bus!!, enable)
    }

    fun releaseCamera() {
        if (isFlashlightOn) {
            disableFlashlight()
        }

        camera?.release()
        camera = null

        bus?.unregister(this)
        isFlashlightOn = false
        shouldStroboscopeStop = true
    }

}
