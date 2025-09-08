package mb.fw.policeminwon.web.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import mb.fw.policeminwon.entity.ViewBillingDetailEntity;

@Mapper
public interface ViewBillingDetailMapper {

	ViewBillingDetailEntity selectBillingDetailByElecPayNo(@Param("elecPayNo") String elecPayNo);
}
