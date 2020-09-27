package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/19 8:58
 */
@Slf4j
@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private UploadProperties uploadProperties;

    public String uploadImage(MultipartFile file) {
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (!uploadProperties.getAllowTypes().contains(contentType)) {
                throw new LyException(ExceptionEnum.INVALID_IMAGE_TYPE);
            }
            // 检查文件内容
            BufferedImage bi = ImageIO.read(file.getInputStream());
            if (bi == null) {
                throw new LyException(ExceptionEnum.INVALID_IMAGE_TYPE);
            }
            // 上传到fdfs
            String extension = StringUtils.substringBeforeLast(file.getOriginalFilename(), ".");
            StorePath storePath = client.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            return uploadProperties.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            log.error("上传文件失败");
            throw new LyException(ExceptionEnum.UPLOAD_IMAGE_FILE);
        }
    }
}
