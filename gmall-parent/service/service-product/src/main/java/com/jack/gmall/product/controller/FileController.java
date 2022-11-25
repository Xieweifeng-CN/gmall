package com.jack.gmall.product.controller;

import com.jack.gmall.common.result.Result;
import com.jack.gmall.product.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/25
 * @Description :
 **/
@RestController
@RequestMapping("/admin/product")
public class FileController {

    @Value("${fileServer.url}")
    private String url;

    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam MultipartFile file){
        //文件上传
        String path = FileUtil.fileUpload(file);
        if (StringUtils.isEmpty(path)){
            return Result.fail();
        }
        // ip+端口+图片地址
        return Result.ok(url + path);
    }
}
