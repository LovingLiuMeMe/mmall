package cn.lovingliu.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-08
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
