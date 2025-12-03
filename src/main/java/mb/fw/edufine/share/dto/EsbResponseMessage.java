package mb.fw.edufine.share.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsbResponseMessage {

	private String esb_interface_id;

	private String esb_transaction_id;

	private String esb_trg_sys;

	private String esb_src_sys;

	private String esb_target_service_url;

	private String esb_crypto_yn;

	private Map<String, Object> esb_param;

	private String esb_sr_cd;

	private String esb_src_org;

	private String esb_trg_org;

	private Integer esb_result_cnt;

	private String esb_result_cd;

	private String esb_result_msg;

	private List<Map<String, Object>> esb_data;

}
