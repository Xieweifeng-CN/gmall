package com.jack.gmall.product.service;

/**
 * 分布式锁的测试service
 */
public interface TestRedisService {

    /**
     * redis锁同步发送测试
     */
    public void sendRedis();

    /**
     * 使用redission操作redis
     */
    public void setByRedission();

}
