declare namespace API {
  type BlogAuthorVO = {
    id?: number
    nickname?: string
    avatar?: string
    gender?: number
    tag?: string
    introduction?: string
  }

  type BlogCommentDTO = {
    id?: number
    parentId?: number
    content?: string
  }

  type BlogCommentVO = {
    id?: number
    commentId?: number
    userId?: number
    username?: string
    userAvatar?: string
    createTime?: string
    content?: string
    parentId?: number
    parentUsername?: string
  }

  type BlogCreateDTO = {
    title?: string
    content?: string
    pictureEditDTOList?: PictureEditDTO[]
  }

  type BlogEditDTO = {
    id?: number
    title?: string
    content?: string
  }

  type BlogLikeOrCollectDTO = {
    id?: number
  }

  type BlogLikeOrCollectVO = {
    count?: number
    haveBean?: boolean
  }

  type BlogListDTO = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userId?: number
    searchText?: string
    isRecommend?: number
    sort?: number
    reviewStatus?: number
    reviewMessage?: string
    reviewerId?: number
    upToDate?: boolean
  }

  type BlogsVO = {
    id?: number
    userId?: number
    title?: string
    content?: string
    viewCount?: number
    likeCount?: number
    commentCount?: number
    collectCount?: number
    isRecommend?: number
    pictureVOList?: PictureListVO[]
    user?: BlogAuthorVO
  }

  type changeRoleParams = {
    userId: number
  }

  type collectionsToggleParams = {
    blogLikeOrCollectDTO: BlogLikeOrCollectDTO
  }

  type commentsParams = {
    blogCommentDTO: BlogCommentDTO
  }

  type delBlogParams = {
    id: number
  }

  type delete1Params = {
    id: number
  }

  type delete2Params = {
    id: number
    spaceId?: number
  }

  type editSpaceParams = {
    spaceId: number
    name: string
  }

  type getBlogDetailParams = {
    id: number
  }

  type getByIdParams = {
    id: number
  }

  type getSpaceInfoParams = {
    spaceId: number
  }

  type kickOutMemberParams = {
    spaceId: number
    userId: number
  }

  type likeToggleParams = {
    blogLikeOrCollectDTO: BlogLikeOrCollectDTO
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type PageBlogsVO = {
    records?: BlogsVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageBlogsVO
    searchCount?: PageBlogsVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PagePictureListVO = {
    records?: PictureListVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePictureListVO
    searchCount?: PagePictureListVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageSpaceUserVO = {
    records?: SpaceUserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageSpaceUserVO
    searchCount?: PageSpaceUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageUser = {
    records?: User[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUser
    searchCount?: PageUser
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PicCategoryDTO = {
    id?: number
    categoryName?: string
    isAsc?: boolean
  }

  type PicCategoryVO = {
    id?: number
    categoryName?: string
  }

  type PictureEditDTO = {
    id?: number
    blogId?: number
    introduction?: string
    categoryId?: number
    tags?: string
    spaceId?: number
  }

  type PictureListVO = {
    id?: number
    thumbUrl?: string
    tags?: string
    collectCount?: number
  }

  type PictureQueryListDTO = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    blogId?: number
    searchText?: string
    categoryId?: number
    pictureType?: number
    spaceId?: number
  }

  type PictureUploadVO = {
    id?: number
    thumbUrl?: string
  }

  type ResultBlogCommentVO = {
    code?: number
    data?: BlogCommentVO
    message?: string
  }

  type ResultBlogLikeOrCollectVO = {
    code?: number
    data?: BlogLikeOrCollectVO
    message?: string
  }

  type ResultBlogsVO = {
    code?: number
    data?: BlogsVO
    message?: string
  }

  type ResultBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type ResultListPicCategoryVO = {
    code?: number
    data?: PicCategoryVO[]
    message?: string
  }

  type ResultLong = {
    code?: number
    data?: number
    message?: string
  }

  type ResultObject = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type ResultPageBlogsVO = {
    code?: number
    data?: PageBlogsVO
    message?: string
  }

  type ResultPagePictureListVO = {
    code?: number
    data?: PagePictureListVO
    message?: string
  }

  type ResultPageSpaceUserVO = {
    code?: number
    data?: PageSpaceUserVO
    message?: string
  }

  type ResultPicCategoryVO = {
    code?: number
    data?: PicCategoryVO
    message?: string
  }

  type ResultPictureUploadVO = {
    code?: number
    data?: PictureUploadVO
    message?: string
  }

  type ResultString = {
    code?: number
    data?: string
    message?: string
  }

  type ResultUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type SendCodeDTO = {
    email?: string
    phone?: string
  }

  type SpaceUserEditDTO = {
    userId?: number
    spaceId?: number
    spaceRole?: number
  }

  type SpaceUserQueryDTO = {
    id?: number
    spaceId?: number
    userId?: number
    spaceRole?: number
  }

  type SpaceUserVO = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    spaceId?: number
    userId?: number
    userName?: string
    spaceRole?: string
    createTime?: string
    updateTime?: string
  }

  type upgradeSpaceParams = {
    spaceId: number
    level: number
  }

  type uploadByUrlParams = {
    url: string
    spaceId?: number
  }

  type uploadParams = {
    spaceId?: number
  }

  type User = {
    id?: number
    email?: string
    phone?: string
    password?: string
    nickname?: string
    avatar?: string
    role?: number
    status?: number
    gender?: number
    tag?: string
    introduction?: string
    createTime?: string
    updateTime?: string
    lastLoginTime?: string
    deleteTime?: string
  }

  type UserEditDTO = {
    email?: string
    phone?: string
    nickname?: string
    gender?: number
    introduction?: string
  }

  type UserLoginByCodeDTO = {
    emailOrPhone?: string
    code?: string
  }

  type UserLoginByPasswordDTO = {
    account?: string
    password?: string
  }

  type UserQueryDTO = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    email?: string
    phone?: string
    nickname?: string
    role?: number
    status?: number
    gender?: number
    tag?: string
    introduction?: string
    createTime?: string
    updateTime?: string
    lastLoginTime?: string
    deleteTime?: string
  }

  type UserVO = {
    id?: number
    email?: string
    phone?: string
    nickname?: string
    avatar?: string
    role?: number
    status?: number
    gender?: number
    tag?: string
    introduction?: string
    createTime?: string
    updateTime?: string
    lastLoginTime?: string
    deleteTime?: string
  }
}
