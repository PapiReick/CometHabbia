/*package com.cometproject.server.network.nitro;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class MessageInterceptorHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception{
        if(in.toString(CharsetUtil.UTF_8).startsWith("GET")){
            ctx.pipeline().addAfter("messageInterceptor","websocketHandler", new MyCustomWebsocketToContent());
            ctx.pipeline().addAfter("messageInterceptor","protocolHandler", new WebSocketServerProtocolHandler("/", true));
        }
    }
}
*/