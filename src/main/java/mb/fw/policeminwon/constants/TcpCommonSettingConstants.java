package mb.fw.policeminwon.constants;

import java.nio.charset.Charset;

public class TcpCommonSettingConstants {
	private TcpCommonSettingConstants() {
	}

	// tcp socket 통신을 위한 byte encoding 설정
	public static Charset MESSAGE_CHARSET;
	
	// tcp socket 통신 logging pretty 설정
	public static boolean PRETTY_LOGGING;
}
