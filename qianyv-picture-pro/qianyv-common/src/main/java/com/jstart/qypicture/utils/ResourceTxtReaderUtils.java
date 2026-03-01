package com.jstart.qypicture.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResourceTxtReaderUtils {
    // 读取prompt目录下的txt文件并返回String
    public static String loadPromptFromResource(String path){
        // 路径：resources/prompt/文件名
        try {
            ClassPathResource resource = new ClassPathResource(path);
            byte[] contentBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取资源文件出错，路径：{}", path);
            throw new RuntimeException(e);
        }
    }
}