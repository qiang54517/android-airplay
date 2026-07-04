package com.example.airplay.server

import com.example.airplay.server.handlers.*
import com.example.airplay.utils.AirPlayLogger
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DatagramPacketDecoder
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.rtsp.RtspDecoder
import io.netty.handler.codec.rtsp.RtspEncoder
import java.net.InetSocketAddress

class AudioReceiver(
    private val audioHandler: AudioHandler,
    private val monitor: Any
) : Runnable {

    @Volatile
    var port: Int = 0
        private set

    override fun run() {
        val bootstrap = Bootstrap()
        val workerGroup = NioEventLoopGroup()

        try {
            bootstrap
                .group(workerGroup)
                .channel(NioDatagramChannel::class.java)
                .localAddress(InetSocketAddress(0))
                .handler(object : ChannelInitializer<DatagramChannel>() {
                    override fun initChannel(ch: DatagramChannel) {
                        ch.pipeline().addLast("audioDecoder", DatagramPacketDecoder(AudioDecoder()))
                        ch.pipeline().addLast("audioHandler", audioHandler)
                    }
                })
            val channelFuture = bootstrap.bind().sync()
            port = (channelFuture.channel().localAddress() as InetSocketAddress).port
            AirPlayLogger.d("AudioReceiver bound to port $port")

            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            AirPlayLogger.e("❌ AudioReceiver error: ${e.message}")
            e.printStackTrace()
        } finally {
            synchronized(monitor) { (monitor as java.lang.Object).notify() }
            workerGroup.shutdownGracefully()
        }
    }
}

class VideoReceiver(
    private val videoHandler: VideoHandler,
    private val monitor: Any
) : Runnable {

    @Volatile
    var port: Int = 0
        private set

    override fun run() {
        val serverBootstrap = ServerBootstrap()
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        try {
            serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(InetSocketAddress(0))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        AirPlayLogger.d("VideoReceiver: new video connection from ${ch.remoteAddress()}")
                        ch.pipeline().addLast("videoDecoder", VideoDecoder())
                        ch.pipeline().addLast("videoHandler", videoHandler)
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            val channelFuture = serverBootstrap.bind().sync()
            port = (channelFuture.channel().localAddress() as InetSocketAddress).port
            AirPlayLogger.i("VideoReceiver bound to port $port, waiting for video data...")

            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            AirPlayLogger.e("❌ VideoReceiver error: ${e.message}")
            e.printStackTrace()
        } finally {
            synchronized(monitor) { (monitor as java.lang.Object).notify() }
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}

class AudioControlServer(private val monitor: Any) : Runnable {

    @Volatile
    var port: Int = 0
        private set

    override fun run() {
        val bootstrap = Bootstrap()
        val workerGroup = NioEventLoopGroup()
        val audioControlHandler = AudioControlHandler()

        try {
            bootstrap
                .group(workerGroup)
                .channel(NioDatagramChannel::class.java)
                .localAddress(InetSocketAddress(0))
                .handler(object : ChannelInitializer<DatagramChannel>() {
                    override fun initChannel(ch: DatagramChannel) {
                        ch.pipeline().addLast(audioControlHandler)
                    }
                })
            val channelFuture = bootstrap.bind().sync()
            port = (channelFuture.channel().localAddress() as InetSocketAddress).port
            AirPlayLogger.d("AudioControlServer bound to port $port")

            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            AirPlayLogger.e("❌ AudioControlServer error: ${e.message}")
            e.printStackTrace()
        } finally {
            synchronized(monitor) { (monitor as java.lang.Object).notify() }
            workerGroup.shutdownGracefully()
        }
    }
}

class ControlServer(
    private val config: AirPlayConfig,
    private val consumer: AirPlayConsumer
) : Runnable {

    override fun run() {
        val serverBootstrap = ServerBootstrap()
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        try {
            AirPlayLogger.i("========== STARTING RTSP SERVER ==========")
            AirPlayLogger.i("Starting RTSP server on port ${config.airtunesPort}...")
            AirPlayLogger.i("Server config: ${config.serverName}, width=${config.width}, height=${config.height}, fps=${config.fps}")
            
            serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(InetSocketAddress(config.airtunesPort))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        AirPlayLogger.d("New RTSP connection from: ${ch.remoteAddress()}")
                        
                        // 每次连接都创建新的handler实例，避免使用@Sharable导致的状态共享问题
                        val pipeline = ch.pipeline()
                        pipeline.addLast(RtspDecoder())
                        pipeline.addLast(RtspEncoder())
                        pipeline.addLast(HttpObjectAggregator(64 * 1024))
                        pipeline.addLast(OptionsHandler())
                        pipeline.addLast(PairingHandler(config))
                        pipeline.addLast(FairPlayHandler())
                        pipeline.addLast(RTSPHandler(config.airtunesPort, consumer))
                        pipeline.addLast(HeartBeatHandler())
                        pipeline.addLast(FallbackHandler())
                        pipeline.addLast(ExceptionHandler())
                        
                        // 记录pipeline中的所有handler
                        AirPlayLogger.d("Pipeline initialized for connection: ${ch.remoteAddress()}")
                        AirPlayLogger.d("Pipeline handlers in order: ${pipeline.names()}")
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            
            val channelFuture = serverBootstrap.bind().sync()
            
            val boundPort = (channelFuture.channel().localAddress() as InetSocketAddress).port
            AirPlayLogger.i("✅ RTSP server started successfully on port $boundPort")
            AirPlayLogger.i("✅ Server is now listening for connections...")
            AirPlayLogger.i("========== RTSP SERVER READY ==========")
            
            channelFuture.channel().closeFuture().addListener { future ->
                if (!future.isSuccess) {
                    AirPlayLogger.e("❌ RTSP server closed with error: ${future.cause()?.message}")
                    future.cause()?.printStackTrace()
                } else {
                    AirPlayLogger.i("RTSP server closed normally")
                }
            }
            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            AirPlayLogger.e("❌ RTSP server failed to start on port ${config.airtunesPort}: ${e.message}")
            AirPlayLogger.e("Exception details: ${e.javaClass.name}")
            e.printStackTrace()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}
