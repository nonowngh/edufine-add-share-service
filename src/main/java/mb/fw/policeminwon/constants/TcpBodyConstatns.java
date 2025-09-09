package mb.fw.policeminwon.constants;

import java.time.LocalDate;

public class TcpBodyConstatns {

	private static final String PREFIX = "0137";
    private static final String SUFFIX = "3";
    
    // 즉심 전자납부번호 형태 '013720253 + 일련번호'
    public static String getSJSElecNumType() {
        int currentYear = LocalDate.now().getYear();
        return PREFIX + currentYear + SUFFIX;
    }
}
