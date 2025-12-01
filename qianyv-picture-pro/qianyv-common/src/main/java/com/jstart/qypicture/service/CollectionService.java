package com.jstart.qypicture.service;

import com.jstart.qypicture.enums.CollectionEnum;
import com.jstart.qypicture.model.entity.Collection;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 28435
* @description 针对表【collection(收藏表)】的数据库操作Service
* @createDate 2025-11-07 09:57:55
*/
public interface CollectionService extends IService<Collection> {

    void collectionToggle(Long loginUserId, Long targetId , CollectionEnum collectionEnum);


}
