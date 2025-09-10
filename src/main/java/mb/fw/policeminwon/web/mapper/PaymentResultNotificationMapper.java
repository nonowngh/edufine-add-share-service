package mb.fw.policeminwon.web.mapper;


import org.apache.ibatis.annotations.Mapper;

import mb.fw.policeminwon.entity.PaymentResultNotificationEntity;

@Mapper
public interface PaymentResultNotificationMapper {

	int insertPaymentResultNotification(PaymentResultNotificationEntity entity);
}
