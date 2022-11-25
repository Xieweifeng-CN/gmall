package com.jack.gmall.product.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author :Jack
 * @CreatTime : 2022/10/25
 * @Description : 文件操作相关的工具类
 **/
public class FileUtil {

    /**
     * 在静态代码块中初始化fastDFS的配置: 只加载一次
     */
    static {
        try {
            //读取配置文件的信息
            ClassPathResource resource = new ClassPathResource("fastDFS.conf");
            //进行fastDFS的初始化
            ClientGlobal.init(resource.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    public static String fileUpload(MultipartFile file) {
        try {
            //初始化tracker的连接
            TrackerClient trackerClient = new TrackerClient();
            //通过客户端获取服务端的实例
            TrackerServer trackerServer = trackerClient.getConnection();
            //通过tracker获取storage的信息
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件的名字
            String filename = file.getOriginalFilename();
            //通过storage进行文件的上传
            /**
             * 参数
             * 1.文件的字节码: 从文件取
             * 2.文件的扩展名: 通过文件名取
             * 3.文件的附加参数
             */
            String[] uploadFile = storageClient.upload_file(file.getBytes(), StringUtils.getFilenameExtension(filename), null);
            // [0] = 组名   [1] = 全量路径和文件名
            return uploadFile[0] + "/" + uploadFile[1];
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
