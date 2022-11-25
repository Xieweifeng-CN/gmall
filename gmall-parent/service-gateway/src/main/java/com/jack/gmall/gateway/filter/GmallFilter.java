package com.jack.gmall.gateway.filter;

import com.jack.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description : 网关全局过滤器定义
 **/
@Component
public class GmallFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义的过滤器逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //从请求的参数中获取token
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //从url中获取token
        String token = request.getQueryParams().getFirst("token");
        if (StringUtils.isEmpty(token)){
            //从请求头中获取token
            token = request.getHeaders().getFirst("token");
            if (StringUtils.isEmpty(token)){
                //从cookie中获取
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if(cookies != null){
                    HttpCookie cookiesFirst = cookies.getFirst("token");
                    if (cookiesFirst != null){
                        token = cookiesFirst.getValue();
                    }
                }
            }
        }
        //判断是否取到了token
        if (StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            //没有携带令牌,拒绝掉
            return response.setComplete();
        }
        //携带了令牌, 校验是否被盗用
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        String redisToken = stringRedisTemplate.opsForValue().get(gatwayIpAddress);
        if (StringUtils.isEmpty(redisToken)){
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            //没有携带令牌, 拒绝掉 (未登录)
            return response.setComplete();
        }
        if (!redisToken.equals(token)){
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            //令牌不一致,拒绝掉 (已登录)
            return response.setComplete();
        }
        //将令牌以固定的形式存储到request请求头中
        request.mutate().header("Authorization","bearer " + token);
        return chain.filter(exchange);
    }

    /**
     * 全局过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
