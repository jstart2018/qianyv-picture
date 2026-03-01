package com.jstart.qypicture.constant;

public interface RedisKey {

    // 博客收藏key
    String BLOG_COLLECTION_KEY = "qypicture:blog:collection:";

    // 图片收藏key
    String PICTURE_COLLECTION_KEY = "qypicture:picture:collection:";

    //博客点赞key
    String BLOG_LIKE_KEY = "qypicture:blog:like:";

    //用户关注key
    String USER_FOLLOW_KEY = "qypicture:user:follow:";

    //用户贡献排行榜key
    String USER_CONTRIBUTION_RANK_KEY = "qypicture:user:contribution:rank";

    // 用户点赞的博客key（以用户ID为维度）
    String USER_BLOG_LIKE_KEY = "qypicture:user:blog:like:";

    // 用户收藏的博客key（以用户ID为维度）
    String USER_BLOG_COLLECTION_KEY = "qypicture:user:blog:collection:";

    // 用户收藏的图片key（以用户ID为维度）
    String USER_PICTURE_COLLECTION_KEY = "qypicture:user:picture:collection:";

}
