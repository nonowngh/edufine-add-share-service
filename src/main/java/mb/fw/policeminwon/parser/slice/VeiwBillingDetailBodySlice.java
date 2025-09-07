package mb.fw.policeminwon.parser.slice;

import io.netty.buffer.ByteBuf;
import mb.fw.policeminwon.utils.ByteBufUtils;

public class VeiwBillingDetailBodySlice {

	final static int HEADER_LENGTH = 74;

	// 전자납부번호(body index 0 ~ 19)
	public static String getElecPayNo(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 19);
	}

	// 바디 정보(body index 0 ~ 552)
	public static String getTotalBody(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 552);
	}
}
