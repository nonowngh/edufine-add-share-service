package mb.fw.policeminwon.web.exception;

import lombok.Getter;
import mb.fw.policeminwon.constants.TcpStatusCode;
import mb.fw.policeminwon.web.dto.ESBApiMessage;

@Getter
public class CustomServletException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ESBApiMessage apiMessage;
	private final TcpStatusCode statusCode;

    public CustomServletException(String errorMessage, ESBApiMessage apiMessage, TcpStatusCode statusCode) {
        super(errorMessage);
        this.apiMessage = apiMessage;
        this.statusCode = statusCode;
    }

}
