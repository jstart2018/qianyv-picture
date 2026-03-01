package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.mapper.PubPictureMapper;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.entity.Collection;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.service.CollectionService;
import com.jstart.qypicture.mapper.CollectionMapper;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

/**
* @author 28435
* @description 针对表【collection(收藏表)】的数据库操作Service实现
* @createDate 2025-11-07 09:57:55
*/
@Service
@Slf4j
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection>
    implements CollectionService{

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private PubPictureMapper pubPictureMapper;
    @Resource
    private BlogService blogService;


    //锁对象
    private final ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectionToggle(Long loginUserId, Long targetId , CollectionEnum collectionEnum) {
        synchronized (lockMap.computeIfAbsent(loginUserId, k -> new Object())) {
            //查询该收藏记录是否存在
            log.info("用户{}操作收藏，目标id:{}，类型:{}-{}", loginUserId, targetId, collectionEnum.getValue(),collectionEnum.getDesc());
            Collection isExists = this.lambdaQuery()
                    .eq(Collection::getTargetId,targetId)
                    .eq(Collection::getContentType, collectionEnum.getValue())
                    .eq(Collection::getUserId, loginUserId)
                    .one();
            String redisKey = "unknown";
            if (collectionEnum.equals(CollectionEnum.BLOG)) {
                redisKey = RedisKey.BLOG_COLLECTION_KEY + targetId;
            }else if (collectionEnum.equals(CollectionEnum.PICTURE)){
                redisKey = RedisKey.PICTURE_COLLECTION_KEY + targetId;
            }
            if (isExists != null) { //已存在，删除
                boolean remove = this.removeById(isExists.getId());
                //如果redis里面存在这个用户则正常。如果不存在，说明并发异常，
                ThrowUtils.throwIf(!remove, ResultEnum.SYSTEM_ERROR, "操作失败，请稍后再试");
                redisTemplate.opsForSet().remove(redisKey, loginUserId);

                // 从用户维度的收藏key中移除目标ID
                if (collectionEnum.equals(CollectionEnum.BLOG)) {
                    redisTemplate.opsForSet().remove(RedisKey.USER_BLOG_COLLECTION_KEY + loginUserId, targetId);
                } else if (collectionEnum.equals(CollectionEnum.PICTURE)) {
                    redisTemplate.opsForSet().remove(RedisKey.USER_PICTURE_COLLECTION_KEY + loginUserId, targetId);
                }

                //同步数据库 收藏数-1
                if (redisKey.contains(RedisKey.BLOG_COLLECTION_KEY)){
                    log.info("用户{}取消收藏博客{}, redisKey:{}", loginUserId, targetId, redisKey);
                    blogService.lambdaUpdate().setSql("collect_count = collect_count - 1").eq(Blog::getId, targetId).update();
                }else if (redisKey.contains(RedisKey.PICTURE_COLLECTION_KEY)){
                    log.info("用户{}取消收藏图片{}, redisKey:{}", loginUserId, targetId, redisKey);
                    pubPictureMapper.updateCollectCount(targetId,-1L);
                }
                return;
            }
            //不存在，新增
            Collection collection = new Collection();
            collection.setTargetId(targetId);
            collection.setContentType(collectionEnum.getValue());
            collection.setUserId(loginUserId);
            boolean save = this.save(collection);
            //redis增加该用户
            redisTemplate.opsForSet().add(redisKey, loginUserId);

            // 向用户维度的收藏key中添加目标ID
            if (collectionEnum.equals(CollectionEnum.BLOG)) {
                redisTemplate.opsForSet().add(RedisKey.USER_BLOG_COLLECTION_KEY + loginUserId, targetId);
            } else if (collectionEnum.equals(CollectionEnum.PICTURE)) {
                redisTemplate.opsForSet().add(RedisKey.USER_PICTURE_COLLECTION_KEY + loginUserId, targetId);
            }

            //同步数据库 收藏数+1
            if (redisKey.contains(RedisKey.BLOG_COLLECTION_KEY)){
                log.info("用户{}点击收藏博客{}, redisKey:{}", loginUserId, targetId, redisKey);
                blogService.lambdaUpdate().setSql("collect_count = collect_count + 1").eq(Blog::getId, targetId).update();
            }else if (redisKey.contains(RedisKey.PICTURE_COLLECTION_KEY)){
                log.info("用户{}点击收藏图片{}, redisKey:{}", loginUserId, targetId, redisKey);
                pubPictureMapper.updateCollectCount(targetId,1L);
            }


            ThrowUtils.throwIf(!save, ResultEnum.SYSTEM_ERROR, "操作失败，请稍后再试");
        }
    }


}




