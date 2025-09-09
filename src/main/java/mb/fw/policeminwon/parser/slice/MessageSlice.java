package mb.fw.policeminwon.parser.slice;

import io.netty.buffer.ByteBuf;
import mb.fw.policeminwon.utils.ByteBufUtils;

public class MessageSlice {

	final static int HEADER_LENGTH = 74;

	public static String getTransactionCode(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, 14, 6);
	}

	public static String getSrFlag(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, 23, 1);
	}

	public static String getHeaderMessage(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, 4, 70);
	}

	// 전자납부번호(body index 0 ~ 19)
	public static String getElecPayNo(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 19);
	}

	// 바디 정보(body length 630)
	public static String getVeiwBillingDetailTotalBody(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 630);
	}

	// 바디 정보(body length 153)
	public static String getPaymentResultNotificationTotalBody(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 153);
	}

	// 바디 정보(body length 86)
	public static String getCancelPaymentTotalBody(ByteBuf buf) {
		return ByteBufUtils.getStringfromBytebuf(buf, HEADER_LENGTH, 86);
	}
}
