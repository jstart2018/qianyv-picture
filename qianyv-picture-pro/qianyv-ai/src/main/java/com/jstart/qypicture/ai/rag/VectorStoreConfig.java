package com.jstart.qypicture.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

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
                .prefix("ai.rag")                  // Optional: defaults to "embedding:"
                .initializeSchema(true)                   // Optional: defaults to false
//                .batchingStrategy(new TokenCountBatchingStrategy()) // Optional: defaults to TokenCountBatchingStrategy
                .build();
    }

    @Bean
    public Advisor retrievalAugmentationAdvisor(VectorStore redisVectorStore){
        log.info("初始化 RetrievalAugmentationAdvisor...");
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .topK(5)
                        .vectorStore(redisVectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

    }

}
