package mb.fw.policeminwon.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ESBApiRequest {

	String interfaceId;
	
	String transactionId;
	
	String bodyData;
}
