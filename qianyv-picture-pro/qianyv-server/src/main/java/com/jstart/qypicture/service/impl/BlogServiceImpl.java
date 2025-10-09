package com.jstart.qypicture.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jstart.qypicture.model.entity.Blog;
import com.jstart.qypicture.service.BlogService;
import com.jstart.qypicture.mapper.BlogMapper;
import org.springframework.stereotype.Service;

/**
 * @author 28435
 * @description 针对表【blog(博客表)】的数据库操作Service实现
 * @createDate 2025-10-08 15:25:16
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

}




