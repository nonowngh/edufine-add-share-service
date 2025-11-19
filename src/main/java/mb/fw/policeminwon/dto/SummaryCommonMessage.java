package mb.fw.policeminwon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryCommonMessage<T> {

	private T inputData;

	private T outputData;

	private String resCode;

	private String resMsg;

}
