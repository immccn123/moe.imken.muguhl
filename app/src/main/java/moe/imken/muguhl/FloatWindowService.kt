package moe.imken.muguhl

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import moe.imken.muguhl.settings.Config
import moe.imken.muguhl.settings.ConfigManager
import kotlin.math.max

class FloatWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var imageView: ImageView
    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Float = 0f // 上一次位置的X.Y坐标
    private var lastY: Float = 0f

    private var isResizing = false
    private var resizeStartX = 0
    private var resizeStartY = 0
    private var resizeStartWidth = 0
    private var resizeStartHeight = 0

    private val minWidth = 100
    private val minHeight = 100

    private val resizeRegionSize = 75

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): FloatWindowService = this@FloatWindowService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @SuppressLint("ClickableViewAccessibility", "RtlHardcoded")
    override fun onCreate() {
        super.onCreate()

        windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        imageView = ImageView(applicationContext)

        val configManager = ConfigManager(applicationContext)
        val config = configManager.getCurrentConfig()

        imageView.setBackgroundColor(config.color)

        val width = config.width
        val height = config.height

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        layoutParams =
            WindowManager.LayoutParams(width, height, type, flags, PixelFormat.TRANSPARENT)

        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams.x = config.x
        layoutParams.y = config.y

        windowManager.addView(imageView, layoutParams)

        imageView.setOnTouchListener { _, event ->
            configManager.updateConfig(
                Config(
                    config.id,
                    config.name,
                    layoutParams.x,
                    layoutParams.y,
                    layoutParams.width,
                    layoutParams.height,
                    config.color
                )
            )
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    if (isInResizeRegion(event)) {
                        isResizing = true
                        resizeStartX = event.rawX.toInt()
                        resizeStartY = event.rawY.toInt()
                        resizeStartWidth = layoutParams.width
                        resizeStartHeight = layoutParams.height
                    } else {
                        isResizing = false
                    }
                    windowManager.updateViewLayout(imageView, layoutParams)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isResizing) {
                        val deltaX = (event.rawX - resizeStartX).toInt()
                        val deltaY = (event.rawY - resizeStartY).toInt()
                        val newWidth = resizeStartWidth + deltaX
                        val newHeight = resizeStartHeight + deltaY
                        layoutParams.width = max(minWidth, newWidth)
                        layoutParams.height = max(minHeight, newHeight)
                        windowManager.updateViewLayout(imageView, layoutParams)
                    } else {
                        val nowX = event.rawX
                        val nowY = event.rawY
                        val tranX = nowX - lastX
                        val tranY = nowY - lastY
                        layoutParams.x += tranX.toInt()
                        layoutParams.y += tranY.toInt()
                        windowManager.updateViewLayout(imageView, layoutParams)
                        lastX = nowX
                        lastY = nowY
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    isResizing = false
                    true
                }

                else -> false
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::imageView.isInitialized) {
            windowManager.removeView(imageView)
        }
    }

    private fun isInResizeRegion(event: MotionEvent): Boolean {
        return event.rawX >= (layoutParams.width - resizeRegionSize) && event.rawY >= (layoutParams.height - resizeRegionSize)
    }
}
