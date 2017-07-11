package com.best.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by zz1987 on 2017/7/11.
 */
public class HeatbeatServer {

    private int port;

    private static final int TIME_OUT = 6;
    private static final int MAX_TIME = 3;

    ServerBootstrap b;
    ChannelFuture f;

    public HeatbeatServer(int port){
        this.port = port;
    }

    public void start(){
        b = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        try{
            b.group(boss, work).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("client "+ch.localAddress().getHostName());
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HeartbeatServerInitializer());
                        }
                    });
            f = b.bind(8080).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    public void stop(){
        if(f!=null){
            f.channel().close();
        }
    }

    public static void main(String[] args) {
        HeatbeatServer heatbeatServer = new HeatbeatServer(1666);
        heatbeatServer.start();
    }
}
