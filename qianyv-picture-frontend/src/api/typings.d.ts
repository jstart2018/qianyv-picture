declare namespace API {
  type addCategoryParams = {
    categoryName: string
  }

  type BlogAuthorVO = {
    id?: number
    nickname?: string
    avatar?: string
    gender?: number
    tag?: string
    introduction?: string
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
    blogId?: number
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

  type checkBlogLikeParams = {
    blogId: number
  }

  type checkCollect1Params = {
    blogId: number
  }

  type checkCollectParams = {
    id: number
  }

  type checkFollowParams = {
    userId: number
  }

  type checkHasSpaceParams = {
    userId: number
  }

  type collectToggleParams = {
    id: number
  }

  type CommentAddDTO = {
    content?: string
    blogId?: number
    parentId?: number
    replyToUserId?: number
  }

  type CommentQueryVO = {
    id?: number
    content?: string
    blogId?: number
    userInfoVO?: UserInfoVO
    parentCommon?: CommentQueryVO
    likeCount?: number
    replyToUser?: UserInfoVO
    createTime?: string
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

  type doChatParams = {
    input: string
    conversationId: number
  }

  type editSpaceParams = {
    spaceId: number
    name: string
  }

  type followToggleParams = {
    userId: number
  }

  type getBlogDetailParams = {
    id: number
  }

  type getByIdParams = {
    id: number
  }

  type getOneByIdParams = {
    id: number
    spaceId?: number
  }

  type getSpaceInfoParams = {
    spaceId: number
  }

  type getUserSpaceListParams = {
    spaceRole: number[]
  }

  type kickOutMemberParams = {
    spaceId: number
    userId: number
  }

  type listParentCommonParams = {
    blogId: number
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

  type PictureDownLoadDTO = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    pictureId?: number
    spaceId?: number
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
    picScale?: number
    introduction?: string
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

  type PictureVO = {
    id?: number
    userId?: number
    thumbUrl?: string
    introduction?: string
    categoryId?: number
    tags?: string
    picSize?: number
    picWidth?: number
    picHeight?: number
    picScale?: number
    picFormat?: string
    createTime?: string
    collectCount?: number
    downLoadCount?: number
    userInfoVO?: UserInfoVO
  }

  type removeCommonParams = {
    id: number
  }

  type Result = {
    code?: number
    data?: Record<string, any>
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

  type ResultListCommentQueryVO = {
    code?: number
    data?: CommentQueryVO[]
    message?: string
  }

  type ResultListPicCategoryVO = {
    code?: number
    data?: PicCategoryVO[]
    message?: string
  }

  type ResultListSpaceVO = {
    code?: number
    data?: SpaceVO[]
    message?: string
  }

  type ResultListUserInfoVO = {
    code?: number
    data?: UserInfoVO[]
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

  type ResultPictureVO = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type ResultSpaceVO = {
    code?: number
    data?: SpaceVO
    message?: string
  }

  type ResultString = {
    code?: number
    data?: string
    message?: string
  }

  type ResultUserInfoVO = {
    code?: number
    data?: UserInfoVO
    message?: string
  }

  type SendCodeDTO = {
    email?: string
    phone?: string
  }

  type ServerSentEventString = true

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

  type SpaceVO = {
    id?: number
    spaceName?: string
    spaceLevel?: number
    maxSize?: number
    maxCount?: number
    totalSize?: number
    totalCount?: number
    userId?: number
    role?: number
    memberCount?: number
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

  type UserInfoVO = {
    id?: number
    nickname?: string
    avatar?: string
    tag?: string
    introduction?: string
    downloadCount?: number
    likeCount?: number
    collectCount?: number
    fanCount?: number
    publishCount?: number
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
}
