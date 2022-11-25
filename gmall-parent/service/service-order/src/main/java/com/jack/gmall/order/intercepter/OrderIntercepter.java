package com.jack.gmall.order.intercepter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/9
 * @Description :
 * 拦截器: 拦截服务器向外部发送的请求
 * 过滤器: 过滤外部向服务器发送的请求
 **/
@Component
public class OrderIntercepter implements RequestInterceptor {

    /**
     * 触发时机: feign调用发生之前
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //获取原始请求对象
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null){
            //获取原始请求对象的请求体
            HttpServletRequest request = requestAttributes.getRequest();
            //获取原始请求体中的所有请求头参数
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()){
                //获取请求头中的key
                String key = headerNames.nextElement();
                //获取请求头中的value
                String value = request.getHeader(key);
                //存储的feign的http请求头中
                requestTemplate.header(key,value);
            }
        }
    }
}
