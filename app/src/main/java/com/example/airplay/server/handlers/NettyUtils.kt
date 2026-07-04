package com.example.airplay.server.handlers

import io.netty.buffer.ByteBuf
import java.io.ByteArrayInputStream
import java.io.InputStream

fun ByteBuf.toInputStream(): InputStream {
    val bytes = ByteArray(this.readableBytes())
    this.getBytes(this.readerIndex(), bytes)
    return ByteArrayInputStream(bytes)
}

fun Any.waitNotify() {
    (this as java.lang.Object).notify()
}

fun Any.waitForNotify() {
    (this as java.lang.Object).wait()
}
