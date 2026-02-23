package com.jstart.qypicture.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.constant.RedisKey;
import com.jstart.qypicture.constant.UserConstant;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.exception.BusinessException;
import com.jstart.qypicture.model.dto.ChangePasswordDTO;
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
import com.jstart.qypicture.utils.COSUtil.CosClientConfig;
import com.jstart.qypicture.utils.COSUtil.CosManager;
import com.jstart.qypicture.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;


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
                .get(UserConstant.USER_ACCOUNT_CODE_KEY + userLoginByCodeDTO.getEmailOrPhone());

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
        redisTemplate.delete(UserConstant.USER_ACCOUNT_CODE_KEY + userLoginByCodeDTO.getEmailOrPhone());
        log.info("用户登录成功, 用户id: {}", user.getId());

    }

    @Override
    public void loginWithPassword(UserLoginByPasswordDTO userLoginByPasswordDTO) {
        // 查询用户是否存在
        User user = new User();
        user.setPassword(SaSecureUtil.sha256(userLoginByPasswordDTO.getPassword()));
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

        UserInfoVO userVOInfo = userMapper.getUserVOInfo(user.getId(), null, null, null, null);

        return userVOInfo;
    }

    @Override
    public List<UserInfoVO> getUserVOList(List<User> userList) {
        return userList.stream().map(this::getUserInfoVO).toList();
    }

    /**
     * 无锁模式：
     * 关注/取关 切换
     *
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
                redisTemplate.opsForSet().remove(redisKey, followUser.getId());
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

    /**
     * 获取贡献榜用户（按贡献值降序排列）
     * @return 贡献榜用户列表（前10名）
     */
    @Override
    public List<UserInfoVO> getHotUser() {
        // 从Redis ZSet获取贡献值最高的前10名用户ID（按分数降序）
        Set<Object> hotUserIdSet = redisTemplate.opsForZSet()
                .range(RedisKey.USER_CONTRIBUTION_RANK_KEY, 0, 9);
        
        if (hotUserIdSet == null || hotUserIdSet.isEmpty()) {
            return Collections.emptyList();
        }


        // 转换为Long列表
        List<Long> hotUserIdList = hotUserIdSet.stream()
                .map(id -> Long.parseLong(String.valueOf(id)))
                .toList()
                .reversed();// 因为ZSet是按分数升序排列的，所以需要反转列表以获得降序

        
        // 批量查询用户信息
        List<User> userList = this.lambdaQuery()
                .in(User::getId, hotUserIdList)
                .list();
        
        // 按Redis中的顺序排序（保持排行榜顺序）
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        
        // 按Redis顺序转换为VO
        return hotUserIdList.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(this::getUserInfoVO)
                .toList();
    }

    /**
     * 更换用户头像
     * @param file 头像文件
     * @return 新头像URL
     */
    @Override
    public String updateAvatar(MultipartFile file) {
        // 1. 获取当前登录用户
        long loginUserId = StpUtil.getLoginIdAsLong();
        User user = this.getById(loginUserId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");

        // 2. 校验文件
        ThrowUtils.throwIf(file == null || file.isEmpty(), ResultEnum.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小（3MB）
        long maxSize = 3 * 1024 * 1024L;
        ThrowUtils.throwIf(file.getSize() > maxSize, ResultEnum.PARAMS_ERROR, "头像文件不能大于2MB");
        // 校验文件格式
        String originalFilename = file.getOriginalFilename();
        ThrowUtils.throwIf(originalFilename == null, ResultEnum.PARAMS_ERROR, "文件名不能为空");
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowSuffixList = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
        ThrowUtils.throwIf(!allowSuffixList.contains(suffix), ResultEnum.PARAMS_ERROR, "仅支持 png, jpg, jpeg, gif, webp 格式的图片");

        // 3. 构建上传路径
        String fileName = String.format("%s_%s.%s",
                LocalDate.now(),
                UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12),
                suffix);
        String key = String.format("avatar/%s/%s", loginUserId, fileName);

        // 4. 上传新头像到COS
        File tempFile = null;
        try {
            tempFile = File.createTempFile("avatar_", "." + suffix);
            file.transferTo(tempFile);
            cosManager.putObject(key, tempFile);
        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultEnum.SYSTEM_ERROR, "头像上传失败，请稍后重试");
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }

        // 5. 构建新头像URL
        String newAvatarUrl = cosClientConfig.getHost() + "/" + key;

        // 6. 删除旧头像（仅删除COS上的头像）
        String oldAvatar = user.getAvatar();
        if (oldAvatar != null && oldAvatar.startsWith(cosClientConfig.getHost())) {
            // 从COS URL中提取key
            String oldKey = oldAvatar.substring(cosClientConfig.getHost().length() + 1);
            try {
                cosManager.deleteObject(oldKey);
                log.info("删除旧头像成功: {}", oldKey);
            } catch (Exception e) {
                // 删除失败不影响主流程，仅记录日志
                log.warn("删除旧头像失败: {}, 错误: {}", oldKey, e.getMessage());
            }
        }

        // 7. 更新用户头像URL
        User updateUser = new User();
        updateUser.setId(loginUserId);
        updateUser.setAvatar(newAvatarUrl);
        boolean updateResult = this.updateById(updateUser);
        ThrowUtils.throwIf(!updateResult, ResultEnum.SYSTEM_ERROR, "更新头像失败，请稍后重试");

        return newAvatarUrl;
    }

    /**
     * 发送修改密码验证码
     * @param type 验证方式：email-邮箱, phone-手机号
     */
    @Override
    public void sendPasswordCode(String type) {
        // 1. 参数校验
        ThrowUtils.throwIf(StringUtils.isBlank(type), ResultEnum.PARAMS_ERROR, "请选择验证方式");
        ThrowUtils.throwIf(!"email".equals(type) && !"phone".equals(type),
                ResultEnum.PARAMS_ERROR, "验证方式参数错误");

        // 2. 获取当前登录用户
        long loginUserId = StpUtil.getLoginIdAsLong();
        User user = this.getById(loginUserId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");

        // 3. 检查用户是否绑定了该方式
        String account;
        if ("email".equals(type)) {
            account = user.getEmail();
            ThrowUtils.throwIf(StringUtils.isBlank(account), ResultEnum.PARAMS_ERROR, "您未绑定邮箱，请选择其他方式");
        } else {
            account = user.getPhone();
            ThrowUtils.throwIf(StringUtils.isBlank(account), ResultEnum.PARAMS_ERROR, "您未绑定手机号，请选择其他方式");
        }

        // 4. 发送验证码
        SendCodeDTO sendCodeDTO = new SendCodeDTO();
        if ("email".equals(type)) {
            sendCodeDTO.setEmail(account);
        } else {
            sendCodeDTO.setPhone(account);
        }
        this.sendCode(sendCodeDTO);
    }

    /**
     * 修改密码
     * @param changePasswordDTO 修改密码请求参数
     */
    @Override
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        // 1. 参数校验
        ThrowUtils.throwIf(changePasswordDTO == null, ResultEnum.PARAMS_ERROR, "参数不能为空");
        String type = changePasswordDTO.getType();
        String code = changePasswordDTO.getCode();
        String newPassword = changePasswordDTO.getNewPassword();

        ThrowUtils.throwIf(StringUtils.isBlank(type), ResultEnum.PARAMS_ERROR, "请选择验证方式");
        ThrowUtils.throwIf(!"email".equals(type) && !"phone".equals(type),
                ResultEnum.PARAMS_ERROR, "验证方式参数错误");
        ThrowUtils.throwIf(StringUtils.isBlank(code), ResultEnum.PARAMS_ERROR, "验证码不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(newPassword), ResultEnum.PARAMS_ERROR, "新密码不能为空");

        // 2. 校验新密码格式（16位以下，需要数字和字母组合）
        ThrowUtils.throwIf(newPassword.length() > 16 || newPassword.length() < 6,
                ResultEnum.PARAMS_ERROR, "密码长度必须在6-16位之间");
        boolean hasLetter = newPassword.matches(".*[a-zA-Z].*");
        boolean hasDigit = newPassword.matches(".*\\d.*");
        ThrowUtils.throwIf(!hasLetter || !hasDigit,
                ResultEnum.PARAMS_ERROR, "密码必须包含数字和字母");

        // 3. 获取当前登录用户
        long loginUserId = StpUtil.getLoginIdAsLong();
        User user = this.getById(loginUserId);
        ThrowUtils.throwIf(user == null, ResultEnum.NOT_FOUND_ERROR, "用户不存在");

        // 4. 获取用户对应的邮箱/手机号
        String account;
        if ("email".equals(type)) {
            account = user.getEmail();
            ThrowUtils.throwIf(StringUtils.isBlank(account), ResultEnum.PARAMS_ERROR, "您未绑定邮箱");
        } else {
            account = user.getPhone();
            ThrowUtils.throwIf(StringUtils.isBlank(account), ResultEnum.PARAMS_ERROR, "您未绑定手机号");
        }

        // 5. 校验验证码
        Object codeFromRedis = redisTemplate.opsForValue().get(UserConstant.USER_ACCOUNT_CODE_KEY + account);
        ThrowUtils.throwIf(codeFromRedis == null, ResultEnum.PARAMS_ERROR, "验证码已过期，请重新获取");
        String codeStr = String.valueOf(codeFromRedis);
        ThrowUtils.throwIf(!codeStr.equals(code), ResultEnum.PARAMS_ERROR, "验证码错误");

        // 6. 更新密码
        User updateUser = new User();
        updateUser.setId(loginUserId);
        updateUser.setPassword(SaSecureUtil.sha256(newPassword));
        boolean updateResult = this.updateById(updateUser);
        ThrowUtils.throwIf(!updateResult, ResultEnum.SYSTEM_ERROR, "修改密码失败，请稍后重试");

        // 7. 删除验证码
        redisTemplate.delete(UserConstant.USER_ACCOUNT_CODE_KEY + account);

        log.info("用户[{}]修改密码成功", loginUserId);
    }

}
