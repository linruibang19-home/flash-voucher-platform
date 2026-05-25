package org.javaup.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.order.entity.RollbackFailureLog;
import org.javaup.order.mapper.RollbackFailureLogMapper;
import org.javaup.order.service.IRollbackFailureLogService;
import org.springframework.stereotype.Service;

/**
 * @description: 回滚失败日志服务实现
 * @maintainer: lrb
 **/
@Service
public class RollbackFailureLogServiceImpl
        extends ServiceImpl<RollbackFailureLogMapper, RollbackFailureLog>
        implements IRollbackFailureLogService {
}
