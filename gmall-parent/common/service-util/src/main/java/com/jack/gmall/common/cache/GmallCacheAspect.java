package com.jack.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 切面缓存增强
 */
@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * `@Around声明环绕增强的入口为一个注解,参数为指定的注解对象
     * @param point
     * @return
     */
    @Around("@annotation(com.jack.gmall.common.cache.Java0509Cache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){
        //作为通用增强,返回对象使用object
        Object result = null;
        try {
            //获取切点(增强方法)的参数, 参数为数组
            Object[] args = point.getArgs();
            //获取切点(增强方法)的签名,签名包含有方法返回类型
            MethodSignature signature = (MethodSignature) point.getSignature();
            //签名拿到具体的方法,此方法需被以下注解声明
            Java0509Cache gmallCache = signature.getMethod().getAnnotation(Java0509Cache.class);
            //获取注解的前缀参数,前缀写增强方法名如: getSkuInfo
            String prefix = gmallCache.prefix();
            //getSkuInfo + [id] , 数组转String后会加[ ], 如: getSkuInfo[1]
            String key = prefix+Arrays.asList(args).toString();
            //②调用自定义方法, 此方法为获取缓存数据
            result = cacheHit(signature, key);
            //判断缓存是否有数据
            if (result!=null){
                // 缓存有数据
                return result;
            }
            //缓存无数据,需使用锁访问数据库
            //初始化分布式锁, 锁的key和
            RLock lock = redissonClient.getLock(key+":lock");
            //取锁, 100s重复取锁, 100s锁有效期
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)){
                //取锁成功
               try {
                   try {
                       //执行节点方法,存入节点参数
                       //从数据库中获取数据
                       result = point.proceed(point.getArgs());
                       //数据库没有数据
                       if (null==result){
                           //创建一个空对象
                           Object o = new Object();
                           //存入redis, 防止缓存穿透
                           this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(o));
                           return null;
                       }
                       //数据库中有数据
                       //把结果放入缓存
                       this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result));
                       return result;
                   } catch (Throwable throwable) {
                       throwable.printStackTrace();
                   }
               }catch (Exception e){
                   e.printStackTrace();
               }finally {
                   //释放锁
                   lock.unlock();
               }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取缓存中的数据
     * @param signature
     * @param key
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        // 1. 查询缓存 key = getSkuInfo[1]
        String cache = (String)redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cache)) {
            // 有，则反序列化，直接返回
            // 获取方法返回类型 如: SkuInfo.class
            Class returnType = signature.getReturnType();
            // 反序列化. 序列化为: java对象转String, 反序列化: String转java对象
            return JSONObject.parseObject(cache, returnType);
        }
        //无数据
        return null;
    }
}
