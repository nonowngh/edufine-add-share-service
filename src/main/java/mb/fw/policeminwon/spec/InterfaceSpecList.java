package mb.fw.policeminwon.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InterfaceSpecList {
	@JsonProperty("interfaceInfo")
	private List<InterfaceSpec> interfaceInfo;

	/**
	 * sndCode, rcvCode, messageCode에 맞는 InterfaceInfo 찾기
	 */
	public InterfaceSpec findInterfaceInfo(String sndCode, String rcvCode, String messageCode) {
		return interfaceInfo.stream()
				.filter(info -> info.getSndCode().equals(sndCode) && info.getRcvCode().equals(rcvCode)
						&& info.getMessageCode().equals(messageCode))
				.findFirst().orElseThrow(() -> new IllegalStateException("해당하는 인터페이스 정보를 찾을 수 없습니다. " + "sndCode: "
						+ sndCode + ", rcvCode: " + rcvCode + ", messageCode: " + messageCode));
	}

	/**
	 * interfaceId에 맞는 InterfaceInfo 찾기
	 */
	public InterfaceSpec findInterfaceInfo(String interfaceId) {
		return interfaceInfo.stream().filter(info -> info.getInterfaceId().equals(interfaceId)).findFirst().orElseThrow(
				() -> new IllegalStateException("해당하는 인터페이스 정보를 찾을 수 없습니다. " + "interfaceId: " + interfaceId));
	}
}
