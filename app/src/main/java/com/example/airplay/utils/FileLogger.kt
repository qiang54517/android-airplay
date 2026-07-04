package com.example.airplay.utils

import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val MAX_LOG_SIZE = 500 * 1024  // 500KB
    
    private val logFile: File by lazy {
        val file = File("/sdcard/Download/airplay_log.txt")
        if (file.exists() && file.length() > MAX_LOG_SIZE) {
            file.delete()
        }
        file
    }
    
    fun log(message: String) {
        try {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            val logMessage = "[$timestamp] $message\n"
            
            FileWriter(logFile, true).use { writer ->
                writer.write(logMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun clear() {
        try {
            logFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
