package com.jack.gmall.product.service.impl;

import com.jack.gmall.common.config.RedissonConfig;
import com.jack.gmall.product.service.TestRedisService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/28
 * @Description :
 **/
@Service
public class TestRedisServiceImpl implements TestRedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * redis锁同步发送测试
     */
    @Override
    public void sendRedis(){
        //②防止线程卡住, 有效期过了, 而被其他线程误删锁, 设置一个随机的值
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //setnx只要key为空时才能写入数据, ①并且保证释放锁失败, 加上有效期
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
        if (lock){
            //成功存值
            Integer i = (Integer) redisTemplate.opsForValue().get("java0509");
            if (i != null){
                i++;
                redisTemplate.opsForValue().set("java0509",i);
            }
            //lua脚本初始化
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end ");
            script.setResultType(Long.class);
            //一步到位,就算删锁前卡住,重运行后,也不会误删除锁
            redisTemplate.execute(script, Arrays.asList("lock"),uuid);
        }else {
            //失败重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendRedis();
        }
    }

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 使用redission操作redis
     */
    @Override
    public void setByRedission() {
        //获取锁
        RLock lock = redissonClient.getLock("lock");
        try {
            //参数一: 指定时间内一直加锁, 参数二: 锁有效期
            if (lock.tryLock(100,100,TimeUnit.SECONDS)){
                try {
                    //加锁成功
                    //从redis中获取某个key
                    Integer i = (Integer) redisTemplate.opsForValue().get("java0509");
                    //判断是否为空
                    if (i != null){
                        i++;
                        redisTemplate.opsForValue().set("java0509",i);
                    }
                } catch (Exception e) {
                    System.out.println("加锁成功,但操作出现异常");
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("加锁失败");
            e.printStackTrace();
        }
    }
}
