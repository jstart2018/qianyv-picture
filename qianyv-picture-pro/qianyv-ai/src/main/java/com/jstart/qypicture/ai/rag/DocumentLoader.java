package com.jstart.qypicture.ai.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DocumentLoader {

    /**
     * 资源模式解析器，用于加载 多篇文档
     */
    private final ResourcePatternResolver resourcePatternResolver;

    public DocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkDown() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 使用资源模式解析器加载 classpath 下的所有 Markdown 文档
            Resource[] resources = resourcePatternResolver.getResources("classpath:documents/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();// 获取文件名
                String apply = filename.substring(filename.lastIndexOf("-") + 1, filename.lastIndexOf("."));
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("type", "功能介绍")
                        .withAdditionalMetadata("apply", apply)//适用人群
                        .withAdditionalMetadata("filename", filename)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("加载文档失败: {}", e.getMessage(), e);
        }
        return allDocuments;
    }
}
