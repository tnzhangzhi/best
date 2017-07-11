package com.best.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by zz1987 on 2017/7/11.
 */
public class HeartbeatServerHandler extends SimpleChannelInboundHandler<HearbeatMessage> {

    private  int un_rec_times = 0;

    ThreadLocal<HearbeatMessage> localmessage = new ThreadLocal<HearbeatMessage>();

    protected void channelRead0(ChannelHandlerContext ctx, HearbeatMessage msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + " Say : sn=" + msg.getSn()+",reqcode="+msg.getReqCode());
        if(msg != null && !"".equals(msg.getSn()) && msg.getReqCode()==1){
            msg.setReqCode(2);
            ctx.channel().writeAndFlush(msg);
            // 失败计数器清零
            un_rec_times = 0;
            if(localmessage.get()==null){
                HearbeatMessage localMsg = new HearbeatMessage();
                localMsg.setSn(msg.getSn());
                localmessage.set(localMsg);
                    /*
                     * 这里可以将设备号放入一个集合中进行统一管理
                     */
                // TODO
            }
        }else{
            ctx.channel().close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                    /*读超时*/
                System.out.println("===服务端===(READER_IDLE 读超时)");
                // 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
                if(un_rec_times >= 3){
                    System.out.println("===服务端===(读超时，关闭chanel)");
                    // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                    ctx.channel().close();
                }else{
                    // 失败计数器加1
                    un_rec_times++;
                }
            } else if (event.state() == IdleState.WRITER_IDLE) {
                    /*写超时*/
                System.out.println("===服务端===(WRITER_IDLE 写超时)");
            } else if (event.state() == IdleState.ALL_IDLE) {
                    /*总超时*/
                System.out.println("===服务端===(ALL_IDLE 总超时)");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("错误原因："+cause.getMessage());
        if(localmessage.get()!=null){
                /*
                 * 从管理集合中移除设备号等唯一标示，标示设备离线
                 */
            // TODO
        }
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client active ");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 关闭，等待重连
        ctx.close();
        if(localmessage.get()!=null){
                /*
                 * 从管理集合中移除设备号等唯一标示，标示设备离线
                 */
            // TODO
        }
        System.out.println("===服务端===(客户端失效)");
    }


}
