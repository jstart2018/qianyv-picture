package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.model.entity.Collection;
import com.jstart.qypicture.service.CollectionService;
import com.jstart.qypicture.mapper.CollectionMapper;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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


    //锁对象
    private final ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

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
            ThrowUtils.throwIf(!save, ResultEnum.SYSTEM_ERROR, "操作失败，请稍后再试");
        }
    }

}




