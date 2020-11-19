package com.mrfox.arrirtty.remoting.handler;

import com.mrfox.arrirtty.remoting.common.RemotingHelper;
import com.mrfox.arrirtty.remoting.common.RemotingUtil;
import com.mrfox.arrirtty.remoting.model.RemoteModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*****
 * 字节转Model
 * @author     : MrFox
 * @date       : 2020-09-02 16:31
 * @description:
 * @version    :
 ****/
@Slf4j
public class NettyByte2RemoteModelHandler extends LengthFieldBasedFrameDecoder {

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength    the maximum length of the frame.  If the length of the frame is
     *                          greater than this value, {@link TooLongFrameException} will be
     *                          thrown.
     * @param lengthFieldOffset the offset of the length field
     * @param lengthFieldLength
     */
    public NettyByte2RemoteModelHandler(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength      the maximum length of the frame.  If the length of the frame is
     *                            greater than this value, {@link TooLongFrameException} will be
     *                            thrown.
     * @param lengthFieldOffset   the offset of the length field
     * @param lengthFieldLength   the length of the length field
     * @param lengthAdjustment    the compensation value to add to the value of the length field
     * @param initialBytesToStrip
     */
    public NettyByte2RemoteModelHandler(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    /**
     * Creates a new instance.
     *
     * @param maxFrameLength      the maximum length of the frame.  If the length of the frame is
     *                            greater than this value, {@link TooLongFrameException} will be
     *                            thrown.
     * @param lengthFieldOffset   the offset of the length field
     * @param lengthFieldLength   the length of the length field
     * @param lengthAdjustment    the compensation value to add to the value of the length field
     * @param initialBytesToStrip the number of first bytes to strip out from the decoded frame
     * @param failFast            If <tt>true</tt>, a {@link TooLongFrameException} is thrown as
     *                            soon as the decoder notices the length of the frame will exceed
     *                            <tt>maxFrameLength</tt> regardless of whether the entire frame
     *                            has been read.  If <tt>false</tt>, a {@link TooLongFrameException}
     *                            is thrown after the entire frame that exceeds <tt>maxFrameLength</tt>
     */
    public NettyByte2RemoteModelHandler(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    /**
     * Creates a new instance.
     *
     * @param byteOrder           the {@link ByteOrder} of the length field
     * @param maxFrameLength      the maximum length of the frame.  If the length of the frame is
     *                            greater than this value, {@link TooLongFrameException} will be
     *                            thrown.
     * @param lengthFieldOffset   the offset of the length field
     * @param lengthFieldLength   the length of the length field
     * @param lengthAdjustment    the compensation value to add to the value of the length field
     * @param initialBytesToStrip the number of first bytes to strip out from the decoded frame
     * @param failFast            If <tt>true</tt>, a {@link TooLongFrameException} is thrown as
     *                            soon as the decoder notices the length of the frame will exceed
     *                            <tt>maxFrameLength</tt> regardless of whether the entire frame
     *                            has been read.  If <tt>false</tt>, a {@link TooLongFrameException}
     *                            is thrown after the entire frame that exceeds <tt>maxFrameLength</tt>
     */
    public NettyByte2RemoteModelHandler(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }


    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param   ctx             the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param   in              the {@link ByteBuf} from which to read data
     * @return  frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                          be created.
     */
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();

            return RemoteModel.decode(byteBuffer);
        } catch (Exception e) {
            log.error("[NettyByte2RemoteModel#decode]解码异常, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingUtil.closeChannel(ctx.channel());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }


}
