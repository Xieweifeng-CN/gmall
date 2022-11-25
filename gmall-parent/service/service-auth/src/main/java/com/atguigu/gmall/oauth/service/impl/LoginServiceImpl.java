package com.atguigu.gmall.oauth.service.impl;

import com.atguigu.gmall.oauth.service.LoginService;
import com.atguigu.gmall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

/**
 * @Author :Jack
 * @CreatTime : 2022/11/7
 * @Description : 用户登录接口实现类
 **/
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /**
     * 登录校验
     *  @param username
     * @param password
     * @return
     */
    @Override
    public AuthToken login(String username, String password) {
        //校验参数
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            throw new RuntimeException("用户名和密码不能为空!");
        }
        //向固定地址发送post请求, 5个参数
        ServiceInstance choose = loadBalancerClient.choose("service-oauth");
        String url = choose.getUri().toString()+"/oauth/token";
        //请求头初始化 两个参数: 客户端id, 密钥
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.set("Authorization",getHeaderParam());
        //请求体初始化 三个参数: 用户名, 用户密码, 授权类型
        MultiValueMap<String,String> body = new HttpHeaders();
        body.set("grant_type","password");
        body.set("username",username);
        body.set("password",password);
        HttpEntity httpEntity = new HttpEntity(body,headers);
        /**
         * 1.请求的地址
         * 2.请求的方式
         * 3.请求的参数
         * 4.返回的数据类型
         */
        ResponseEntity<Map> exchange =
                restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        //获取需要的令牌信息, 6个
        Map<String, String> result = exchange.getBody();
        //自定义pojo
        AuthToken authToken = new AuthToken();
        //获取令牌
        String accessToken = result.get("access_token");
        authToken.setAccessToken(accessToken);
        //获取刷新令牌
        String refreshToken = result.get("refresh_token");
        authToken.setRefreshToken(refreshToken);
        //获取jti
        String jti = result.get("jti");
        authToken.setJti(jti);
        //生成令牌,返回
        return authToken;
    }

    /**
     * 拼接请求头Authorization信息
     * @return
     */
    private String getHeaderParam() {
        // Basic amF2YTA1MDk6YXRndWlndQ==
        // amF2YTA1MDk6YXRndWlndQ== 等于 客户端id:密钥
        // 拼接
        String s = clientId + ":" + clientSecret;
        // base64加密
        byte[] encode = Base64.getEncoder().encode(s.getBytes());
        // 返回
        return "Basic " + new String(encode);
    }
}
