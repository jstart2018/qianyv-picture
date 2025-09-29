package com.qypicture.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qypicture.user.entity.User;
import com.qypicture.user.service.UserService;
import com.qypicture.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 28435
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-09-24 22:53:52
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




