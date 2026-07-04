package com.example.airplay.protocol

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.airplay.R

object AirPlayLookupTables {
    lateinit var tableS1: ByteArray
    lateinit var tableS2: ByteArray
    lateinit var tableS3: ByteArray
    lateinit var tableS4: ByteArray
    lateinit var tableS5: IntArray
    lateinit var tableS6: IntArray
    lateinit var tableS7: IntArray
    lateinit var tableS8: IntArray
    lateinit var tableS9: IntArray
    lateinit var tableS10: ByteArray

    fun init(context: Context) {
        val res = context.resources
        try {
            tableS1 = res.openRawResource(R.raw.table_s1).readBytes()
            tableS2 = res.openRawResource(R.raw.table_s2).readBytes()
            tableS3 = res.openRawResource(R.raw.table_s3).readBytes()
            tableS4 = res.openRawResource(R.raw.table_s4).readBytes()
            tableS5 = readInts(res.openRawResource(R.raw.table_s5))
            tableS6 = readInts(res.openRawResource(R.raw.table_s6))
            tableS7 = readInts(res.openRawResource(R.raw.table_s7))
            tableS8 = readInts(res.openRawResource(R.raw.table_s8))
            tableS9 = readInts(res.openRawResource(R.raw.table_s9))
            tableS10 = res.openRawResource(R.raw.table_s10).readBytes()
        } catch (e: Exception) {
            android.util.Log.e("AirPlayLookupTables", "Failed to load lookup tables", e)
            // Provide empty fallbacks so app doesn't crash
            tableS1 = ByteArray(0)
            tableS2 = ByteArray(0)
            tableS3 = ByteArray(0)
            tableS4 = ByteArray(0)
            tableS5 = IntArray(0)
            tableS6 = IntArray(0)
            tableS7 = IntArray(0)
            tableS8 = IntArray(0)
            tableS9 = IntArray(0)
            tableS10 = ByteArray(0)
        }
    }

    private fun readInts(inputStream: java.io.InputStream): IntArray {
        val lines = mutableListOf<String>()
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
        }
        return lines.map { java.lang.Long.decode(it).toInt() }.toIntArray()
    }
}
