package com.jack.gmall.order.filter;

import com.jack.gmall.order.util.OrderThreadLocalUtil;
import com.jack.gmall.order.util.TokenUtil;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/8
 * @Description :
 **/
@WebFilter(filterName = "cartFilter", urlPatterns = "/*")
@Order(1) //执行优先级, 值越小优先级越高
public class OrderFilter extends GenericFilter {

    /**
     * 自定义过滤器的逻辑
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //获取请求头中的参数
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("Authorization").replace("bearer ","");
        //校验token
        Map<String, String> map = TokenUtil.decodeToken(token);
        if (map != null){
            //获取用户名
            String username = map.get("username");
            //存到本地线程的集合中  map(当前线程id, username)
            OrderThreadLocalUtil.set(username);
        }
        //方行
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
