package org.javaup.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.javaup.dto.VoucherReconcileLogDto;
import org.javaup.enums.LogType;
import org.javaup.message.SeckillVoucherMessage;
import org.javaup.message.MessageExtend;
import org.javaup.order.entity.VoucherReconcileLog;
import org.javaup.order.mapper.VoucherReconcileLogMapper;
import org.javaup.order.service.IVoucherReconcileLogService;
import org.javaup.toolkit.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @description: 对账日志服务实现
 * @maintainer: lrb
 **/
@Service
public class VoucherReconcileLogServiceImpl
        extends ServiceImpl<VoucherReconcileLogMapper, VoucherReconcileLog>
        implements IVoucherReconcileLogService {

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReconcileLog(Integer logType, Integer businessType, String detail,
                                    MessageExtend<SeckillVoucherMessage> message) {
        SeckillVoucherMessage body = message.getMessageBody();
        VoucherReconcileLogDto dto = buildDto(logType, businessType, detail, body.getTraceId(), message.getUuid(), body);
        if (dto.getLogType().equals(LogType.RESTORE.getCode())) {
            dto.setBeforeQty(body.getAfterQty());
            dto.setAfterQty(body.getBeforeQty());
        }
        return saveReconcileLog(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReconcileLog(Integer logType, Integer businessType, String detail,
                                    Long traceId, MessageExtend<SeckillVoucherMessage> message) {
        SeckillVoucherMessage body = message.getMessageBody();
        VoucherReconcileLogDto dto = buildDto(logType, businessType, detail, traceId, message.getUuid(), body);
        if (dto.getLogType().equals(LogType.RESTORE.getCode())) {
            dto.setBeforeQty(body.getAfterQty());
            dto.setAfterQty(body.getBeforeQty());
        }
        return saveReconcileLog(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReconcileLog(VoucherReconcileLogDto dto) {
        VoucherReconcileLog entity = new VoucherReconcileLog();
        entity.setId(snowflakeIdGenerator.nextId())
                .setOrderId(dto.getOrderId())
                .setUserId(dto.getUserId())
                .setVoucherId(dto.getVoucherId())
                .setMessageId(dto.getMessageId())
                .setBusinessType(dto.getBusinessType())
                .setDetail(dto.getDetail())
                .setTraceId(dto.getTraceId())
                .setLogType(dto.getLogType())
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setBeforeQty(dto.getBeforeQty())
                .setChangeQty(dto.getChangeQty())
                .setAfterQty(dto.getAfterQty());
        return save(entity);
    }

    private VoucherReconcileLogDto buildDto(Integer logType, Integer businessType, String detail,
                                            Long traceId, String messageId, SeckillVoucherMessage body) {
        VoucherReconcileLogDto dto = new VoucherReconcileLogDto();
        dto.setOrderId(body.getOrderId());
        dto.setUserId(body.getUserId());
        dto.setVoucherId(body.getVoucherId());
        dto.setMessageId(messageId);
        dto.setDetail(detail);
        dto.setBeforeQty(body.getBeforeQty());
        dto.setChangeQty(body.getChangeQty());
        dto.setAfterQty(body.getAfterQty());
        dto.setTraceId(traceId);
        dto.setLogType(logType);
        dto.setBusinessType(businessType);
        return dto;
    }
}
