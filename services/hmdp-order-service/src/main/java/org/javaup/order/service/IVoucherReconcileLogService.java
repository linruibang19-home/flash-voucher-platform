package org.javaup.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.dto.VoucherReconcileLogDto;
import org.javaup.message.SeckillVoucherMessage;
import org.javaup.message.MessageExtend;
import org.javaup.order.entity.VoucherReconcileLog;

/**
 * @description: 对账日志服务接口
 * @maintainer: lrb
 **/
public interface IVoucherReconcileLogService extends IService<VoucherReconcileLog> {

    boolean saveReconcileLog(Integer logType, Integer businessType, String detail,
                             MessageExtend<SeckillVoucherMessage> message);

    boolean saveReconcileLog(Integer logType, Integer businessType, String detail,
                             Long traceId, MessageExtend<SeckillVoucherMessage> message);

    boolean saveReconcileLog(VoucherReconcileLogDto voucherReconcileLogDto);
}
