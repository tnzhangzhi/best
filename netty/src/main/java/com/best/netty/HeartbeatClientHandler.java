package com.best.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by zz1987 on 2017/7/11.
 */
public class HeartbeatClientHandler extends SimpleChannelInboundHandler<HearbeatMessage> {

    private int unRecPongTimes = 0;
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HearbeatMessage msg) throws Exception {
        System.out.println("Server say : sn=" + msg.getSn()+",reqcode="+msg.getReqCode());
        if (2 == msg.getReqCode()) {
            // 计数器清零
            unRecPongTimes = 0;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client active ");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client close ");
        super.channelInactive(ctx);
            /*
             * 重连
             */
//        if(!isStop){
//            connServer();
//        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                    /*读超时*/
                System.out.println("===服务端===(READER_IDLE 读超时)");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                    /*写超时*/
                System.out.println("===服务端===(WRITER_IDLE 写超时)");
                if(unRecPongTimes < 3){
                    ctx.channel().writeAndFlush(getSrcMsg()) ;
                    unRecPongTimes++;
                }else{
                    ctx.channel().close();
                }
            } else if (event.state() == IdleState.ALL_IDLE) {
                    /*总超时*/
                System.out.println("===服务端===(ALL_IDLE 总超时)");
            }
        }
    }

    private HearbeatMessage getSrcMsg(){
        HearbeatMessage keepAliveMessage = new HearbeatMessage();
        // 设备码
        keepAliveMessage.setSn("sn_123456abcdfef");
        keepAliveMessage.setReqCode(1);
        return keepAliveMessage ;
    }

}
