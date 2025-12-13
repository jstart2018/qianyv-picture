package com.jstart.qypicture.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.model.entity.PubPicture;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 应用启动时执行的初始化操作，确保自定义 Bean 已正确注入和初始化
 * 初始化 Redis RAG 数据库
 */
@Component
@Slf4j
public class InitRedisRagDb implements CommandLineRunner {

    @Resource
    private VectorStore redisVectorStore;
//    @Resource
//    private PubPictureMapper pubPictureMapper;

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动初始化，精选图片纳入知识库。。。");

        //todo : 初始化好RAG知识库
/*
        try {
            QueryWrapper<PubPicture> qw = new QueryWrapper<>();
            qw.eq("is_recommend",1);
            //1、 从数据库中获取推荐图片数据
            List<PubPicture> pubPictures = pubPictureMapper.selectList(qw);
            //2、 将推荐图片数据添加到 Redis RAG 向量存储中
            if (pubPictures == null || pubPictures.isEmpty()) {
                log.info("应用启动初始化，精选图片纳入知识库完成，共纳入0条数据。");
                return;
            }

            List<Document> list = pubPictures.stream().map(pic -> {
                String content = pic.getAiDesc() + "链接为：" + pic.getThumbUrl();
                return Document.builder()
                        .text(content)
                        .metadata("picture_id", pic.getId().toString())
                        .metadata("type","图片推荐")
                        .metadata("type","图片")
                        .metadata("thumb_url", pic.getThumbUrl())
                        .build();
            }).toList();
            redisVectorStore.add(list);
            log.info("应用启动初始化，精选图片纳入知识库完成，共纳入{}条数据。", list.size());
        } catch (Exception e) {
            log.error("应用启动初始化，精选图片纳入知识库失败：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

*/
    }
}