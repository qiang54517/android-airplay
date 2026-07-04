package com.example.airplay.server

data class AirPlayConfig(
    val serverName: String = "Android AirPlay",
    val airtunesPort: Int = 7000,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 30
)
