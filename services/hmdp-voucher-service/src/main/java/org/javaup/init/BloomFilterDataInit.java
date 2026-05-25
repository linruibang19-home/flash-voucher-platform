package org.javaup.init;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.javaup.entity.SeckillVoucher;
import org.javaup.handler.BloomFilterHandlerFactory;
import org.javaup.service.ISeckillVoucherService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.javaup.constant.Constant.BLOOM_FILTER_HANDLER_VOUCHER;

@Slf4j
@Order(1)
@Component
public class BloomFilterDataInit {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private BloomFilterHandlerFactory bloomFilterHandlerFactory;

    @PostConstruct
    public void init() {
        log.info("Initialize voucher bloom filter");
        List<SeckillVoucher> seckillVouchers = seckillVoucherService.list();
        for (SeckillVoucher seckillVoucher : seckillVouchers) {
            bloomFilterHandlerFactory.get(BLOOM_FILTER_HANDLER_VOUCHER)
                    .add(String.valueOf(seckillVoucher.getVoucherId()));
        }
    }
}
