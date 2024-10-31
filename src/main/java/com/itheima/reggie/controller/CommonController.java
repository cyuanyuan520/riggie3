package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Random;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    private Random random = new Random();
    /**
     * 上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info(file.toString());
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //获取原始文件后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //拼接新文件名
        String fileName = UUID.randomUUID().toString() + suffix;
        //检测根目录是否存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            log.info(basePath + "不存在, 正在创建...");
            dir.mkdirs();
            log.info(basePath + "创建成功");
        }
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name) {
        try {
            //创建字节缓冲输入流读取文件
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(basePath + name)));
            //创建字节缓冲输出流
            BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
            //两个缓冲流配合实现download
            int len = 0;
            while ((len = bis.read()) != -1) {
                bos.write(len);
            }
            bos.flush();
            bis.close();
            bos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
