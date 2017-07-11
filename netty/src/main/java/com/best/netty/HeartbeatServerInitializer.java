package com.best.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by zz1987 on 2017/7/11.
 */
public class HeartbeatServerInitializer extends ChannelInitializer<SocketChannel> {

    protected void initChannel(SocketChannel sc) throws Exception {
        ChannelPipeline ppl = sc.pipeline();
        ppl.addLast("decoder",new ObjectDecoder(ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
        ppl.addLast("encoder",new ObjectEncoder());

        ppl.addLast("pong",new IdleStateHandler(6,0,0, TimeUnit.SECONDS));
        ppl.addLast("handler",new HeartbeatServerHandler());
    }
}
