package com.example.airplay.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AirPlayLogger {
    private const val TAG = "AirPlay"
    
    fun d(message: String) {
        Log.d(TAG, message)
        LogCollector.log("[DEBUG] $message")
        FileLogger.log("[DEBUG] $message")
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
        LogCollector.log("✅ $message")
        FileLogger.log("✅ $message")
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
        LogCollector.log("⚠️  $message")
        FileLogger.log("⚠️  $message")
    }
    
    fun e(message: String) {
        Log.e(TAG, message)
        LogCollector.log("❌ $message")
        FileLogger.log("❌ $message")
    }
    
    fun logRequest(method: String, uri: String, headers: String, contentLength: Int) {
        val log = """=== INCOMING REQUEST ===
Method: $method
URI: $uri
Headers:
${headers}Content Length: $contentLength
========================"""
        
        Log.d(TAG, log)
        LogCollector.log(log)
        FileLogger.log(log)
    }
    
    fun logResponse(status: String, headers: String, contentLength: Int) {
        val log = """=== OUTGOING RESPONSE ===
Status: $status
Headers:
${headers}Content Length: $contentLength
========================="""
        
        Log.d(TAG, log)
        LogCollector.log(log)
        FileLogger.log(log)
    }
}
