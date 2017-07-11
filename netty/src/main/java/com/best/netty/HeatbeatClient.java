package com.best.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zz1987 on 2017/7/11.
 */
public class HeatbeatClient {

    private int port;
    private String host;

    private EventLoopGroup group;
    private Channel ch;

    Bootstrap b;
    private ScheduledExecutorService executorService;
    private boolean is_stop = false;

    ChannelFuture f;

    public HeatbeatClient(int port,String host){
        this.port = port;
        this.host = host;
        group = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).handler(new HeartbeatClientInitializer());
    }

    public void connect(){
        is_stop = false;

        if(executorService!=null){
            executorService.shutdown();
        }
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(new Runnable() {

            boolean isConnSucc = true;

            public void run() {
                try {
                    // 重置计数器
//                    unRecPongTimes = 0;
                    // 连接服务端
                    if(ch!=null&&ch.isOpen()){
                        ch.close();
                    }
                    ch = b.connect(host, port).sync().channel();
                    // 此方法会阻塞
//                  ch.closeFuture().sync();
                    System.out.println("connect server finish");
                } catch (Exception e) {
                    e.printStackTrace();
                    isConnSucc = false ;
                } finally{
                    if(isConnSucc){
                        if(executorService!=null){
                            executorService.shutdown();
                        }
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void start(){
        connect();
    }

    public void stop(){
        is_stop = true;
        if(ch!=null&&ch.isOpen()){
            ch.close();
        }
        if(executorService!=null){
            executorService.shutdown();
        }
    }

    public static void main(String[] args) {
        HeatbeatClient keepAliveServer = new HeatbeatClient(1666,"127.0.0.1");
        keepAliveServer.start();
    }
}
