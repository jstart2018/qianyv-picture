package com.jstart.qypicture.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;


/**
 * VectorStore: 负责存储和管理向量数据的组件，提供添加、查询和删除向量的功能。
 * RedisVectorStore: VectorStore 的一种实现，使用 Redis 作为底层存储系统，适用于需要高性能和分布式存储的场景。
 * RetrievalAugmentationAdvisor: 一种顾问（Advisor）实现，结合了检索增强（Retrieval Augmentation）技术，能够在生成回答时利用外部知识库进行增强，提高回答的准确性和相关性。
 * VectorStoreDocumentRetriever: 一种文档检索器（Document Retriever）实现，使用 VectorStore 进行文档检索，根据查询向量与存储中的向量进行相似度计算，返回最相关的文档。
 * ContextualQueryAugmenter: 一种查询增强器（Query Augmenter）实现，能够在生成回答时对查询进行增强，提供更多上下文信息，帮助模型更好地理解查询意图和背景。
 */
@Configuration
@Slf4j
public class VectorStoreConfig {

    @Bean
    public JedisPooled jedisPooled() {
        log.info("初始化 JedisPooled...");
        //todo: 上线时可能要适配 用户名密码问题
        return new JedisPooled("localhost", 6379,"default","123456");
    }

    @Bean
    public VectorStore redisVectorStore(JedisPooled jedisPooled, EmbeddingModel siliconflowEmbeddingModel) {
        log.info("初始化 RedisVectorStore...");
        return RedisVectorStore.builder(jedisPooled, siliconflowEmbeddingModel)
//                .indexName("custom-index")                // Optional: defaults to "spring-ai-index"
                .prefix("ai:rag:")                  // Optional: defaults to "embedding:"
                .initializeSchema(true)                   // Optional: defaults to false
//                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }

}
