package mb.fw.policeminwon.parser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mb.fw.policeminwon.constants.ByteEncodingConstants;
import mb.fw.policeminwon.entity.PaymentResultNotificationEntity;
import mb.fw.policeminwon.utils.ByteBufUtils;

/**
 * 경찰청 범칙금 - 과태료 납부결과 통지
 */
public class PaymentResultNotificationParser {
	public static PaymentResultNotificationEntity toEntity(String data) {
		ByteBuf buf = Unpooled.copiedBuffer(data, ByteEncodingConstants.CHARSET);
		PaymentResultNotificationEntity entity = new PaymentResultNotificationEntity();
		int offset = 0;
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setObligorRegNo, buf, offset, 13); // 납부의무자 주민(사업자, 법인) 등록번호 (AN, 13)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setCollectorAccountNo, buf, offset, 6); // 징수관 계좌번호 (AN, 6)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setElecPayNo, buf, offset, 19); // 전자납부번호 (AN, 19)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField1, buf, offset, 3); // 예비정보 FIELD 1 (AN, 3)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField2, buf, offset, 7); // 예비정보 FIELD 2 (AN, 7)
		offset = ByteBufUtils.setIntegerAndMoveOffset(entity::setPayAmount, buf, offset, 15); // 납부 금액 (N, 15)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPayDate, buf, offset, 8); // 납부 일자 (N, 8)
		offset = ByteBufUtils.setIntegerAndMoveOffset(entity::setBankBranchCode, buf, offset, 7); // 출금 금융회사 점별 코드 (N, 7)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField3, buf, offset, 16); // 예비정보 FIELD 3 (AN, 16)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField4, buf, offset, 14); // 예비정보 FIELD 4 (ANS, 14)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPayerRegNo, buf, offset, 13);// 납부자 주민(사업자) 등록 번호 (AN, 13)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField5, buf, offset, 10); // 예비정보 FIELD 5 (AHNS, 10)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField6, buf, offset, 10); // 예비정보 FIELD 6 (AHNS, 10)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPaySystem, buf, offset, 1); // 납부 이용 시스템 (AN, 1)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPrePaySystem, buf, offset, 1); // 기 납부 이용 시스템 (AN, 1)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setPayType, buf, offset, 1); // 납부 형태 구분 (AN, 1)
		offset = ByteBufUtils.setStringAndMoveOffset(entity::setReserveField7, buf, offset, 15); // 예비정보 FIELD 7 (AN, 9)
		return entity;
	}

	public static String toMessage(PaymentResultNotificationEntity entity) {
		ByteBuf buf = Unpooled.buffer();		
		ByteBufUtils.writeRightPaddingString(buf, entity.getObligorRegNo(), 13);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getCollectorAccountNo(), 6);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getElecPayNo(), 19);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField1(), 3);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField2(), 7);
	    ByteBufUtils.writeLeftPaddingNumber(buf, entity.getPayAmount(), 15);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPayDate(), 8);
	    ByteBufUtils.writeLeftPaddingNumber(buf, entity.getBankBranchCode(), 7);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField3(), 16);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField4(), 14);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPayerRegNo(), 13);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField5(), 10);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField6(), 10);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPaySystem(), 1);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPrePaySystem(), 1);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getPayType(), 1);
	    ByteBufUtils.writeRightPaddingString(buf, entity.getReserveField7(), 15);
	    return buf.toString(ByteEncodingConstants.CHARSET);
	}
}