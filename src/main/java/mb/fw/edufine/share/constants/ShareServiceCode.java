package mb.fw.edufine.share.constants;

import java.util.Arrays;

import lombok.Getter;

public enum ShareServiceCode {

	// 국세청 국세납세증명
	NTS_NTP("NTP", "NltTaxpayProofService"),
	// 국세청 전자세금계산서
	NTS_ETB("ETB", "ElctrnTaxBillService"),
	// 행정안정부 지방세납세증명
	MOS_LTP("LTP", "LlxTaxpayProofService");

	@Getter
	private final String systemCode;
	@Getter
	private final String serviceName;

	ShareServiceCode(String systemCode, String serviceName) {
		this.systemCode = systemCode;
		this.serviceName = serviceName;
	}

	public static ShareServiceCode fromSystemCode(String systemCode) {
		return Arrays.stream(ShareServiceCode.values()).filter(value -> value.getSystemCode().equals(systemCode))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid pattern-type code : " + systemCode));
	}
}
