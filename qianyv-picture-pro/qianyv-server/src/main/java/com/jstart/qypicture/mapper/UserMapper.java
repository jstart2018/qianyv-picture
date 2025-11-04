package com.jstart.qypicture.mapper;

import com.jstart.qypicture.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jstart.qypicture.model.vo.UserInfoVO;

/**
 * @author 28435
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2025-10-01 08:08:47
 * @Entity entity.com.jstart.qypicture.model.User
 */
public interface UserMapper extends BaseMapper<User> {

    UserInfoVO getUserInfo(Long id);
}




