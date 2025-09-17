package mb.fw.policeminwon.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESBApiMessage {

	String interfaceId;
	
	String transactionId;
	
	String bodyMessage;
	
	String headerMessage;
	
	String statusCode;
	
	String esbErrorMessage;
	
}
