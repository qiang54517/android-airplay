package com.example.airplay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.IBinder
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.airplay.server.AirPlayConfig
import com.example.airplay.utils.LogCollector
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var statusText: TextView
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var nameInput: EditText
    
    // 日志相关UI
    private lateinit var logToggleBtn: Button
    private lateinit var logScrollView: ScrollView
    private lateinit var logText: TextView
    
    private var isLogVisible = false  // 日志面板是否可见

    private var service: AirPlayService? = null
    private var bound = false
    private var serverRunning = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as AirPlayService.LocalBinder).getService()
            bound = true
            service?.onStateChanged = { updateStatus(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initNormal()
        } catch (e: Exception) {
            showErrorFullScreen(e, "onCreate 初始化异常")
        }
        
        // 设置全局未捕获异常处理器（用于捕获崩溃）
        setupCrashHandler()
    }
    
    /**
     * 崩溃处理器 - 显示完整堆栈跟踪
     */
    private fun setupCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val fullTrace = formatStackTrace(e, "全局崩溃")
            
            // 保存到LogCollector以便在UI中查看
            LogCollector.log("❌ === APP CRASHED ===")
            LogCollector.log(fullTrace)
            
            runOnUiThread { 
                showErrorFullScreen(e, "应用崩溃") 
            }
        }
    }

    private fun initNormal() {
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        statusText = findViewById(R.id.statusText)
        startBtn = findViewById(R.id.startBtn)
        stopBtn = findViewById(R.id.stopBtn)
        nameInput = findViewById(R.id.nameInput)
        
        // 初始化日志UI组件
        logToggleBtn = findViewById(R.id.logToggleBtn)
        logScrollView = findViewById(R.id.logScrollView)
        logText = findViewById(R.id.logText)

        // 配置日志显示
        setupLogDisplay()

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                if (serverRunning && service != null) {
                    try {
                        service?.startServer(Surface(surface as SurfaceTexture), buildConfig())
                    } catch (e: Exception) {
                        showErrorFullScreen(e, "startServer 异常")
                    }
                }
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        startBtn.setOnClickListener {
            try { 
                // 启动前清除旧日志
                LogCollector.clear()
                startAirPlay() 
            } catch (e: Exception) { showErrorFullScreen(e, "启动异常") }
        }
        stopBtn.setOnClickListener { stopAirPlay() }

        bindService(
            Intent(this, AirPlayService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )

        updateStatus("点击\"启动 AirPlay\"开始接收")
    }
    
    /**
     * 配置日志显示区域
     */
    private fun setupLogDisplay() {
        // 日志切换按钮点击事件
        logToggleBtn.setOnClickListener {
            isLogVisible = !isLogVisible
            
            if (isLogVisible) {
                // 显示日志面板
                logScrollView.visibility = android.view.View.VISIBLE
                logToggleBtn.text = "📋 隐藏调试日志"
                
                // 加载现有日志到文本框
                logText.text = LogCollector.getAllLogs()
                
                // 滚动到底部
                logScrollView.post {
                    logScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
            } else {
                // 隐藏日志面板
                logScrollView.visibility = android.view.View.GONE
                logToggleBtn.text = "📋 查看调试日志"
            }
        }
        
        // 长按日志文本可复制全部内容
        logText.setOnLongClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboardManager?.let {
                it.setPrimaryClip(android.content.ClipData.newPlainText("AirPlay Logs", logText.text.toString()))
                updateStatus("✅ 日志已复制到剪贴板")
                true
            } ?: false
        }
        
        // 监听日志更新
        LogCollector.addListener { newLog ->
            if (isLogVisible) {
                // 追加新日志
                val currentText = logText.text?.toString() ?: ""
                if (currentText.isNotEmpty()) {
                    logText.text = "$currentText\n$newLog"
                } else {
                    logText.text = newLog
                }
                
                // 自动滚动到底部
                logScrollView.post {
                    logScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    
                    // 限制文本长度，防止内存溢出
                    val textLength = logText.length()
                    if (textLength > 50000) {
                        // 保留后半部分
                        logText.text = "...[日志过长，已截断]\n${logText.text.subSequence(textLength - 40000, textLength)}"
                    }
                }
            }
        }
        
        // 初始提示
        LogCollector.log("✅ AirPlay 应用已启动")
        LogCollector.log("点击\"启动 AirPlay\"后，这里将显示详细的RTSP协议交互日志")
        LogCollector.log("如果Mac/iPhone连接失败，请将此处的日志发送给开发者分析")
        LogCollector.log("=========================================")
    }

    /** 全屏显示完整错误信息，可滚动、可复制 */
    private fun showErrorFullScreen(e: Throwable, tag: String) {
        val fullTrace = formatStackTrace(e, tag)

        // 1. 显示在屏幕上
        try {
            val scrollView = ScrollView(this).apply {
                setBackgroundColor(Color.parseColor("#1A1A2E"))
                isFillViewport = true
            }

            val label = TextView(this).apply {
                text = "⚠️ 应用错误 - 所有信息均可长按复制\n\n"
                setTextColor(Color.parseColor("#FF6B6B"))
                textSize = 16f
                setPadding(32, 32, 32, 8)
            }

            val traceText = TextView(this).apply {
                text = fullTrace
                setTextColor(Color.parseColor("#CCCCCC"))
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                setTextIsSelectable(true)  // 关键：可复制！
                setPadding(32, 8, 32, 32)
            }

            val container = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                addView(label)
                addView(traceText)
            }

            scrollView.addView(container)
            setContentView(scrollView)
        } catch (_: Exception) {
            // If even the error screen fails, fallback to a simple text view
            try {
                val tv = TextView(this).apply {
                    text = fullTrace
                    setTextColor(Color.WHITE)
                    setTextIsSelectable(true)
                    setPadding(16, 16, 16, 16)
                }
                setContentView(android.widget.ScrollView(this).apply {
                    addView(tv)
                    setBackgroundColor(Color.BLACK)
                })
            } catch (_: Exception) {}
        }

        // 2. 写入文件（可在文件管理器中找到）
        try {
            val dir = getExternalFilesDir(null) ?: filesDir
            val file = File(dir, "crash_${System.currentTimeMillis()}.log")
            file.writeText(fullTrace)
            android.util.Log.e("AirPlayCrash", "Full crash log written to: ${file.absolutePath}")
        } catch (_: Exception) {}
    }

    private fun formatStackTrace(e: Throwable, tag: String): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        pw.println("TAG: $tag")
        pw.println("Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())}")
        pw.println("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        pw.println("Android SDK: ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})")
        pw.println("App Version: ${packageManager.getPackageInfo(packageName, 0).versionName}")
        pw.println()
        pw.println("=== FULL STACK TRACE ===")
        e.printStackTrace(pw)

        // Include cause chain
        var cause = e.cause
        var depth = 0
        while (cause != null && depth < 10) {
            pw.println()
            pw.println("=== CAUSE #${depth + 1}: ${cause.javaClass.name} ===")
            pw.println("Message: ${cause.message}")
            cause.printStackTrace(pw)
            cause = cause.cause
            depth++
        }
        pw.flush()
        return sw.toString()
    }

    private fun startAirPlay() {
        val svc = service ?: return
        val surfaceTexture = textureView.surfaceTexture ?: return

        serverRunning = true
        
        LogCollector.log("========== 用户点击启动 AirPlay ==========")
        LogCollector.log("设备名称: ${nameInput.text}")
        LogCollector.log("端口: 7000")
        LogCollector.log("分辨率: 1920x1080 @30fps")
        
        svc.startServer(Surface(surfaceTexture as SurfaceTexture), buildConfig())

        startBtn.isEnabled = false
        stopBtn.isEnabled = true
        
        LogCollector.log("startServer() 方法已调用")
        LogCollector.log("等待RTSP服务器初始化...")
    }

    private fun stopAirPlay() {
        service?.stopServer()
        serverRunning = false
        startBtn.isEnabled = true
        stopBtn.isEnabled = false
        updateStatus("AirPlay 已停止")
        
        LogCollector.log("用户点击停止 AirPlay")
    }

    private fun buildConfig() = AirPlayConfig(
        serverName = nameInput.text.toString().ifBlank { "Android AirPlay" },
        airtunesPort = 7000,
        width = 1920,
        height = 1080,
        fps = 30
    )

    private fun updateStatus(text: String) {
        runOnUiThread { statusText.text = text }
    }

    override fun onDestroy() {
        stopAirPlay()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        super.onDestroy()
    }
}
