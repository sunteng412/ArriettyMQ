package com.mrfox.arrirtty.remoting.handler;

import com.alibaba.fastjson.JSON;
import com.mrfox.arrirtty.remoting.common.RemotingHelper;
import com.mrfox.arrirtty.remoting.common.RemotingUtil;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/*****
 * Model转byte
 * @author     : MrFox
 * @date       : 2020-09-02 16:31
 * @description:
 * @version    :
 ****/
@Slf4j
public class NettyRemoteModel2ByteHandler extends MessageToByteEncoder<RemoteModel> {

    /**
     * Encode a message into a {@link ByteBuf}. This method will be called for each written message that can be handled
     * by this encoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RemoteModel msg, ByteBuf out) throws Exception {
        try {
            byte[] body = msg.getContent();
            if (body != null) {
                out.writeBytes(JSON.toJSONBytes(msg));
            }
        } catch (Exception e) {
            log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            if (msg != null) {
                log.error(msg.toString());
            }
            RemotingUtil.closeChannel(ctx.channel());
        }
    }
}
