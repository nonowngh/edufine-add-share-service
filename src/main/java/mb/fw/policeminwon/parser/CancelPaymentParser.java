package mb.fw.policeminwon.parser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.constants.TcpCommonSettingConstants;
import mb.fw.policeminwon.entity.CancelPaymentEntity;
import mb.fw.policeminwon.utils.ByteBufUtils;

public class CancelPaymentParser {
	
	public static CancelPaymentEntity toEntity(String data) {
		ByteBuf buf = Unpooled.copiedBuffer(data, TcpCommonSettingConstants.MESSAGE_CHARSET);
		CancelPaymentEntity entity = new CancelPaymentEntity();
		int offset = 0;
		offset = ByteBufUtils.setIntegerAndMoveOffset(entity::setBankBranchCode, buf, offset, 7); // 출금 금융회사 점별 코드 (N, 7)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPayerRegNo, buf, offset, 13); // 납부자 주민(사업자) 등록번호 (AN, 13)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setOriginCenterMsgNo, buf, offset, 12); // 원거래 센터 전문 관리 번호 (AN, 12)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setOriginSendDateTime, buf, offset, 12); // 원거래 전송 일시 (N, 12)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField1, buf, offset, 16); // 예비 정보 FIELD 1 (AN, 16)
		offset = ByteBufUtils.setIntegerAndMoveOffset(entity::setOriginPayAmount, buf, offset, 15); // 원거래 납부 금액 (N, 15)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setCancelReason, buf, offset, 1); // 취소 사유 (AN, 1)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setOriginPayType, buf, offset, 1); // 원거래 납부 형태 구분 (AN, 1)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField2, buf, offset, 9); //예비 정보 FIELD 2 (AN, 9)
		return entity;
	}

	public static String toMessage(CancelPaymentEntity entity) {
		ByteBuf buf = Unpooled.buffer();		
		ByteBufUtils.writeLeftPaddingNumber(buf, entity.getBankBranchCode(), 7);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPayerRegNo(), 13);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getOriginCenterMsgNo(), 12);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getOriginSendDateTime(), 12);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField1(), 16);
	    ByteBufUtils.writeLeftPaddingNumber(buf, entity.getOriginPayAmount(), 15);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getCancelReason(), 1);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getOriginPayType(), 1);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField2(), 9);
	    return buf.toString(TcpCommonSettingConstants.MESSAGE_CHARSET);
	}
}