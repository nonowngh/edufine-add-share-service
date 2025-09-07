package mb.fw.policeminwon;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

public class FixedLengthMessageBuilder {

	public static void main(String[] args) {
		List<byte[]> fields = new ArrayList<>();

		fields.add(toFixedLength("EP1234567890123456", 19));
		fields.add(toFixedLength("RESERVE_FIELD_1", 20));
		fields.add(toFixedLength("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p", 32));
		fields.add(toFixedLength("RESERVE_FIELD_2_DATA", 32));
		fields.add(toFixedLength("M", 1));
		fields.add(toFixedLength("9001011234567", 13));
		fields.add(toFixedLength("1234567890", 10));
		fields.add(toFixedLength("RF3", 3));
		fields.add(toFixedLength("홍길동", 40));
		fields.add(toFixedLength("RF4FIELD01", 10));
		fields.add(toFixedLength("8201011234567", 13));
		fields.add(toFixedLength("PAYER000000000", 15));
		fields.add(toFixedLengthNum(1, 1));
		fields.add(toFixedLength("서울시청", 20));
		fields.add(toFixedLength("123456", 6));
		fields.add(toFixedLengthNum(2, 1));
		fields.add(toFixedLengthNum(500000, 15));
		fields.add(toFixedLengthNum(550000, 15));
		fields.add(toFixedLengthNum(1234567, 7));
		fields.add(toFixedLengthNum(2025, 4));
		fields.add(toFixedLengthNum(20250910, 8));
		fields.add(toFixedLengthNum(20250920, 8));
		fields.add(toFixedLengthNum(20250901103045L, 14));
		fields.add(toFixedLengthNum(20250901103100L, 14));
		fields.add(toFixedLength("서울시 강남구 테헤란로 123", 40));
		fields.add(toFixedLength("불법 주정차로 인한 과태료 부과", 100));
		fields.add(toFixedLength("12가3456", 20));
		fields.add(toFixedLength("도로교통법 제32조 제1항", 100));
		fields.add(toFixedLength("RF5-001", 7));
		fields.add(toFixedLengthNum(20250906120000L, 14));
		fields.add(toFixedLength("1", 1));
		fields.add(toFixedLength("김철수", 8));
		fields.add(toFixedLength("Y", 1));
		fields.add(toFixedLength("RESVFLD6DATA1234", 18));

		// 최종 전문 조립
		int totalLength = fields.stream().mapToInt(b -> b.length).sum();
		byte[] message = new byte[totalLength];
		int pos = 0;
		for (byte[] field : fields) {
			System.arraycopy(field, 0, message, pos, field.length);
			pos += field.length;
		}

		// 결과 출력
		String result = new String(message, Charset.forName("euc-kr"));
		System.out.println(result);
		System.out.println("총 바이트 길이: " + message.length);
	}

	// 문자열 필드 (UTF-8 기준 고정 바이트)
	public static byte[] toFixedLength(String input, int byteLength) {
		if (input == null)
			input = "";
		byte[] bytes = input.getBytes(Charset.forName("euc-kr"));

		if (bytes.length == byteLength) {
			return bytes;
		} else if (bytes.length < byteLength) {
			byte[] padded = new byte[byteLength];
			System.arraycopy(bytes, 0, padded, 0, bytes.length);
			for (int i = bytes.length; i < byteLength; i++) {
				padded[i] = ' ';
			}
			return padded;
		} else {
			return truncateToByteLength(input, byteLength);
		}
	}

	// 숫자 필드
	public static byte[] toFixedLengthNum(long number, int byteLength) {
		String s = String.format("%0" + byteLength + "d", number);
		return s.getBytes(Charset.forName("euc-kr"));
	}

	// UTF-8 바이트 단위로 자르되 깨지지 않게
	private static byte[] truncateToByteLength(String input, int byteLength) {
		CharsetEncoder encoder = Charset.forName("euc-kr").newEncoder();
		encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		encoder.onMalformedInput(CodingErrorAction.IGNORE);

		ByteBuffer buffer = ByteBuffer.allocate(byteLength);
		CharBuffer charBuffer = CharBuffer.wrap(input);
		encoder.encode(charBuffer, buffer, true);
		buffer.flip();

		byte[] result = new byte[buffer.limit()];
		buffer.get(result);
		return result;
	}
}
