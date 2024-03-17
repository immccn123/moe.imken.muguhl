package moe.imken.muguhl

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView

class FloatWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var imageView: ImageView
    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Float = 0f // 上一次位置的X.Y坐标
    private var lastY: Float = 0f
    private var nowX: Float = 0f // 当前移动位置的X.Y坐标
    private var nowY: Float = 0f
    private var tranX: Float = 0f // 悬浮窗移动位置的相对值
    private var tranY: Float = 0f

    private var isResizing = false
    private var resizeStartX = 0
    private var resizeStartY = 0
    private var resizeStartWidth = 0
    private var resizeStartHeight = 0

    private val minWidth = 100 // 设置最小宽度
    private val minHeight = 100 // 设置最小高度

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        imageView = ImageView(applicationContext)

        imageView.setBackgroundColor(Color.BLUE)

        val width = 200
        val height = 400

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            width,
            height,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSPARENT
        )

        layoutParams.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams.x = 0
        layoutParams.y = 0

        windowManager.addView(imageView, layoutParams)

        imageView.setOnTouchListener { _, event ->
            event.let {
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
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (isResizing) {
                            val deltaX = (event.rawX - resizeStartX).toInt()
                            val deltaY = (event.rawY - resizeStartY).toInt()
                            val newWidth = resizeStartWidth + deltaX
                            val newHeight = resizeStartHeight + deltaY
                            if (newWidth >= minWidth && newHeight >= minHeight) {
                                layoutParams.width = newWidth
                                layoutParams.height = newHeight
                                windowManager.updateViewLayout(imageView, layoutParams)
                            }
                        } else {
                            nowX = event.rawX
                            nowY = event.rawY
                            tranX = nowX - lastX
                            tranY = nowY - lastY
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::imageView.isInitialized) {
            windowManager.removeView(imageView)
        }
    }

    private fun isInResizeRegion(event: MotionEvent): Boolean {
        val resizeRegionSize = 100 // 设置调整大小区域的大小
        return event.rawX >= (layoutParams.width - resizeRegionSize) &&
                event.rawY >= (layoutParams.height - resizeRegionSize)
    }
}
