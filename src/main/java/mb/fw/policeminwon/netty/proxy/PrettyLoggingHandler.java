package mb.fw.policeminwon.netty.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import mb.fw.policeminwon.constants.ByteEncodingConstants;

public class PrettyLoggingHandler extends LoggingHandler {

	public PrettyLoggingHandler(LogLevel level) {
		super(level);
	}

	@Override
	protected String format(ChannelHandlerContext ctx, String eventName, Object arg) {
		if (arg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) arg;
			String content = buf.toString(ByteEncodingConstants.CHARSET);
			return String.format("[%s] %s: %s", ctx.channel().id(), eventName, content);
		}
		return super.format(ctx, eventName, arg);
	}
}
