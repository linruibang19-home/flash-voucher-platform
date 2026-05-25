package org.javaup.servicelock.info;

/**
 * @description: 处理失败抽象
 * @maintainer: lrb
 **/
public interface LockTimeOutHandler {
    
    /**
     * 处理
     * @param lockName 锁名
     * */
    void handler(String lockName);
}
