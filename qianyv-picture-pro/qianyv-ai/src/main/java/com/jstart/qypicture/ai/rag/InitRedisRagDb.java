package com.jstart.qypicture.ai.rag;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.entity.PubPicture;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.service.PictureService;
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
    @Resource
    private PictureService pictureService;
    @Resource
    private DocumentLoader documentLoader;

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动初始化，精选图片纳入知识库。。。");

        try {
            PictureQueryListDTO pictureDto = new PictureQueryListDTO();
            pictureDto.setIsRecommend(1);
            //1、 从数据库中获取推荐图片数据
            List<PictureListVO> pictureListVO = pictureService.slectList(pictureDto);
            //2、 将推荐图片数据添加到 Redis RAG 向量存储中
            if (pictureListVO == null || pictureListVO.isEmpty()) {
                log.info("应用启动初始化，精选图片纳入知识库完成，共纳入0条数据。");
            }else {
                List<Document> list = pictureListVO.stream()
                        .filter(pic -> pic.getAiDesc() != null && !pic.getAiDesc().isEmpty() && pic.getThumbUrl() != null && !pic.getThumbUrl().isEmpty())
                        .map(pic -> {
                            String content = "图片说明：" + pic.getAiDesc() + "；链接为：" + pic.getThumbUrl();
                            return Document.builder()
                                    .text(content)
                                    .metadata("picture_id", pic.getId().toString())
                                    .metadata("type", "图片推荐")
                                    .metadata("action", "发送图片")
                                    .build();
                        }).toList();
                redisVectorStore.add(list); // 将推荐图片数据添加到 Redis RAG 向量存储中
                log.info("应用启动初始化，精选图片纳入知识库完成，共纳入{}条图片数据。", list.size());
            }

            //3、 从 Markdown 文件中加载数据
            List<Document> documents = documentLoader.loadMarkDown();
            redisVectorStore.add(documents); // 将 Markdown 文档数据添加到 Redis RAG 向量存储中
            log.info("应用启动初始化，Markdown 文档纳入知识库完成，共纳入{}条文档数据。", documents.size());

        } catch (Exception e) {
            log.error("应用启动初始化，知识库导入失败：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
}