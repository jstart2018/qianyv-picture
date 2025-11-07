package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.constant.UserConstant;
import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.model.entity.Follow;
import com.jstart.qypicture.service.FollowService;
import com.jstart.qypicture.template.sendCodeTemplate.SendCodeTemplate;
import com.jstart.qypicture.template.sendCodeTemplate.SendEmail;
import com.jstart.qypicture.template.sendCodeTemplate.SendSMS;
import com.jstart.qypicture.model.dto.UserLoginByCodeDTO;
import com.jstart.qypicture.model.dto.UserLoginByPasswordDTO;
import com.jstart.qypicture.model.dto.SendCodeDTO;
import com.jstart.qypicture.model.entity.User;
import com.jstart.qypicture.model.vo.UserInfoVO;
import com.jstart.qypicture.service.UserService;
import com.jstart.qypicture.mapper.UserMapper;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 28435
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-10-01 08:08:47
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private SendEmail sendEmail;
    @Resource
    private SendSMS sendSMS;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private FollowService followService;


    //锁对象
    private final ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public void sendCode(SendCodeDTO sendCodeDTO) {
        SendCodeTemplate sendCodeTemplate;

        String account = StringUtils.isNotBlank(sendCodeDTO.getPhone()) ? sendCodeDTO.getPhone() : sendCodeDTO.getEmail();
        //1. 判断账号类型(邮箱或手机号)
        if (account.contains("@")) {
            sendCodeTemplate = sendEmail;
        } else {
            sendCodeTemplate = sendSMS;
        }
        //2. 发送验证码
        sendCodeTemplate.sendCode(account);
    }

    @Override
    public void loginOrRegister(UserLoginByCodeDTO userLoginByCodeDTO) {
        //1. 获取验证码
        Object codeFromRedis = redisTemplate.opsForValue()
                .get(UserConstant.USER_LOGIN_CODE_KEY + userLoginByCodeDTO.getEmailOrPhone());

        // 2. 校验验证码
        if (codeFromRedis == null) {
            throw new BusinessException(ResultEnum.NOT_FOUND_ERROR, "验证码已过期");
        }
        String codeStr = String.valueOf(codeFromRedis);
        if (StringUtils.isBlank(codeStr) || !codeStr.equals(userLoginByCodeDTO.getCode())) {
            throw new BusinessException(ResultEnum.NOT_FOUND_ERROR, "验证码错误");
        }

        //3. 查询用户是否存在
        User user = new User();
        if (userLoginByCodeDTO.getEmailOrPhone().contains("@")) {
            user.setEmail(userLoginByCodeDTO.getEmailOrPhone());
        } else {
            user.setPhone(userLoginByCodeDTO.getEmailOrPhone());
        }
        //4. 不存在则注册
        if (this.count(getQueryWrapper(user)) == 0) {
            user.setNickname("千语" + ((int) ((Math.random() * 9 + 1) * 100000)));
            user.setPassword(userLoginByCodeDTO.getEmailOrPhone());//初始化密码和账号一致
            try {
                this.saveOrUpdate(user);
            } catch (Exception e) {
                log.error("用户注册失败, 用户信息: {}, 错误信息: {}", user.toString(), e.getMessage());
                ThrowUtils.throwIf(true, ResultEnum.SYSTEM_ERROR, "注册失败，请稍后重试");
            }
        }
        user = this.getOne(getQueryWrapper(user));
        //5. 存在则登录
        StpUtil.login(user.getId());

        //6. 删除验证码
        redisTemplate.delete(UserConstant.USER_LOGIN_CODE_KEY + userLoginByCodeDTO.getEmailOrPhone());
        log.info("用户登录成功, 用户id: {}", user.getId());

    }

    @Override
    public void loginWithPassword(UserLoginByPasswordDTO userLoginByPasswordDTO) {
        // 查询用户是否存在
        User user = new User();
        if (userLoginByPasswordDTO.getAccount().contains("@")) {
            user.setEmail(userLoginByPasswordDTO.getAccount());
        } else {
            user.setPhone(userLoginByPasswordDTO.getAccount());
        }
        user = this.getOne(getQueryWrapper(user));
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "账号或密码错误");
        //存在就登录
        StpUtil.login(user.getId());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(User user) {
        Long id = user.getId();
        String email = user.getEmail();
        String phone = user.getPhone();
        String nickname = user.getNickname();
        Integer role = user.getRole();
        Integer status = user.getStatus();
        Integer gender = user.getGender();
        String tag = user.getTag();
        String introduction = user.getIntroduction();

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq(id != null, "id", id);
        qw.eq(!StringUtils.isBlank(email), "email", email);
        qw.eq(!StringUtils.isBlank(phone), "phone", phone);
        qw.like(!StringUtils.isBlank(nickname), "nickname", nickname);
        qw.eq(role != null, "role", role);
        qw.eq(status != null, "status", status);
        qw.eq(gender != null, "gender", gender);
        qw.like(!StringUtils.isBlank(tag), "tag", tag);
        qw.like(!StringUtils.isBlank(introduction), "introduction", introduction);
        return qw;
    }

    /**
     * 获取用户详情，包括分享数、获点赞数等信息
     *
     * @param user
     * @return
     */
    @Override
    public UserInfoVO getUserInfoVO(User user) {

        //todo 查询用户的分享数、获点赞数等信息
        //UserInfoVO userInfoVO = userMapper.getUserInfo(user.getId());
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public List<UserInfoVO> getUserVOList(List<User> userList) {
        return userList.stream().map(this::getUserInfoVO).toList();
    }

    /**
     * 无锁模式：
     * 关注/取关 切换
     * @param userId
     */
    @Override
    public void followToggle(Long userId) {
        User followUser = this.getById(userId);
        User loginUser = this.getById(StpUtil.getLoginIdAsLong());
        ThrowUtils.throwIf(followUser == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");

        String redisKey = RedisKey.USER_FOLLOW_KEY + loginUser.getId();

        // 先查Redis判断状态（减少数据库访问）
        Boolean isInRedis = redisTemplate.opsForSet().isMember(redisKey, followUser.getId());

        if (Boolean.TRUE.equals(isInRedis)) {
            // Redis中存在，尝试删除数据库记录（幂等操作）
            boolean remove = followService.lambdaUpdate()
                    .eq(Follow::getUserId, loginUser.getId())
                    .eq(Follow::getFollowUserId, followUser.getId())
                    .remove();// 直接删除，返回影响行数

            if (remove) {
                // 数据库删除成功，事务提交后删Redis
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.opsForSet().remove(redisKey, followUser.getId());
                    }
                });
                return;
            } else {
                // 数据库已无记录，但Redis有（数据不一致），强制删Redis
                redisTemplate.opsForSet().remove(redisKey,  followUser.getId());
                throw new BusinessException(ResultEnum.SYSTEM_ERROR, "操作失败，请稍后再试");
            }
        } else {
            // Redis中不存在，尝试新增数据库记录
            Follow follow = new Follow();
            follow.setFollowUserId(followUser.getId());
            follow.setUserId(loginUser.getId());
            try {
                boolean save = followService.save(follow);
                ThrowUtils.throwIf(!save, ResultEnum.SYSTEM_ERROR, "操作失败，请稍后再试");
                // 事务提交后加Redis
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.opsForSet().add(redisKey, followUser.getId());
                    }
                });
            } catch (DuplicateKeyException e) { // 捕获唯一索引冲突（并发新增）
                // 并发新增时，数据库会报唯一约束异常，此时查询实际状态并同步Redis
                boolean exists = followService.lambdaQuery()
                        .eq(Follow::getUserId, loginUser.getId())
                        .eq(Follow::getFollowUserId, followUser.getId())
                        .exists();
                if (!exists) {
                    // 实际已存在，同步Redis
                    redisTemplate.opsForSet().add(redisKey, followUser.getId());
                }
                throw new BusinessException(ResultEnum.SYSTEM_ERROR, "操作频繁，请稍后再试");
            }
        }
    }

    @Override
    public Boolean checkFollow(Long userId) {
        try {
            long loginUserId = StpUtil.getLoginIdAsLong();
            Boolean isMember = redisTemplate.opsForSet().isMember(RedisKey.USER_FOLLOW_KEY + loginUserId, userId);
            if (isMember) return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }


}
