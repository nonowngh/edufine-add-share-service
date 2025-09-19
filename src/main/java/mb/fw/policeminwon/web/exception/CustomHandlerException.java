package mb.fw.policeminwon.web.exception;

import lombok.Getter;
import mb.fw.policeminwon.constants.TcpStatusCode;

@Getter
public class CustomHandlerException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final TcpStatusCode statusCode;
	private String systemCode;
	private String headerMessage;

    public CustomHandlerException(String errorMessage, TcpStatusCode statusCode, String systemCode, String headerMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.systemCode = systemCode;
        this.headerMessage = headerMessage;
    }

}
