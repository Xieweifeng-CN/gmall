package com.jack.gmall.order.util;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 购物车微服务的本地线程工具类
 **/
public class OrderThreadLocalUtil {

    /**
     * 定义本地线程对象
     */
    private final static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 获取用户名方法
     * @return
     */
    public static String get(){
        return threadLocal.get();
    }

    /**
     * 存储方法
     * @param username
     */
    public static void set(String username){
        threadLocal.set(username);
    }

}
