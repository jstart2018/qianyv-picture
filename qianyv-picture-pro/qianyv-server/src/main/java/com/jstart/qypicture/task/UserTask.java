package com.jstart.qypicture.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserTask {

    @Resource
    private UserService userService;
    @Resource
    private BlogService blogService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Scheduled(fixedRate = 442000)
    public void updateUserCacheTask() {
        List<User> userList = userService.list();
        userList.forEach(user -> {
            String key = RedisKey.USER_CONTRIBUTION_RANK_KEY;

            Long publishCount = blogService.getBaseMapper()
                    .selectCount(new LambdaQueryWrapper<Blog>()
                            .eq(Blog::getUserId, user.getId()));
            redisTemplate.opsForZSet().add(key, user.getId().toString(), publishCount);
        });
    }
}