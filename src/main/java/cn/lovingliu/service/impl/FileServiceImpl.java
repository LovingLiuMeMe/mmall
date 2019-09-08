package cn.lovingliu.service.impl;

import cn.lovingliu.service.IFileService;
import cn.lovingliu.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author：LovingLiu
 * @Description: 图片线上处理
 * @Date：Created in 2019-09-08
 */
@Service("fileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    public String upload(MultipartFile file,String path){
        // 图片原始名称
        String fileName = file.getOriginalFilename();
        // 图片扩展名
        String fileExtensionName  = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("上传文件的的文件名是: {},上传的路径: {},新文件名: {}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);// 赋予可写权限
            fileDir.mkdirs();// 可创建多个级联文件 如:path = "/a/b/c/d" 就可以创建 a b c d 四个文件夹
        }
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            //文件上传成功
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 已经上传到ftp服务器上
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
