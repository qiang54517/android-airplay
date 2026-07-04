package com.example.airplay.utils

import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日志收集器 - 收集所有AirPlay相关日志并提供给UI显示
 * 使用单例模式，可以在任何地方调用
 */
object LogCollector {
    
    private const val MAX_LOG_LINES = 500  // 最大保存行数
    
    private val logs = mutableListOf<String>()
    private val listeners = mutableListOf<(String) -> Unit>()
    
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    private var isEnabled = true  // 控制是否收集日志
    
    /**
     * 添加日志
     */
    fun log(message: String) {
        if (!isEnabled) return
        
        val timestamp = dateFormat.format(Date())
        val logLine = "[$timestamp] $message"
        
        synchronized(logs) {
            logs.add(logLine)
            
            // 限制日志数量，防止内存溢出
            if (logs.size > MAX_LOG_LINES) {
                logs.removeAt(0)
            }
        }
        
        // 通知所有监听器（在主线程）
        mainHandler.post {
            listeners.forEach { it(logLine) }
        }
    }
    
    /**
     * 获取所有日志
     */
    fun getAllLogs(): String {
        synchronized(logs) {
            return logs.joinToString("\n")
        }
    }
    
    /**
     * 清除所有日志
     */
    fun clear() {
        synchronized(logs) {
            logs.clear()
        }
        
        mainHandler.post {
            listeners.forEach { it("[LOGS CLEARED]") }
        }
    }
    
    /**
     * 添加日志变更监听器
     */
    fun addListener(listener: (String) -> Unit) {
        mainHandler.post {
            listeners.add(listener)
        }
    }
    
    /**
     * 移除日志监听器
     */
    fun removeListener(listener: (String) -> Unit) {
        mainHandler.post {
            listeners.remove(listener)
        }
    }
    
    /**
     * 设置是否启用日志收集
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
