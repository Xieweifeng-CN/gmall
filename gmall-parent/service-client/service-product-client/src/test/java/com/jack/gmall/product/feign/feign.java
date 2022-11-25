package com.jack.gmall.product.feign;

import com.jack.gmall.model.product.SkuInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/27
 * @Description :
 **/
public class feign {

    @Autowired
    private ProductFeign productFeign;

    @Test
    public void test(){
        SkuInfo skuInfo = productFeign.getSkuInfo(36L);
        System.out.println(skuInfo);
    }
}
