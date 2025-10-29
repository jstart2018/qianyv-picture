// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /blog/${param0} */
export async function getBlogDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getBlogDetailParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultBlogsVO>(`/blog/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /blog/collections/toggle */
export async function collectionsToggle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.collectionsToggleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBlogLikeOrCollectVO>('/blog/collections/toggle', {
    method: 'GET',
    params: {
      ...params,
      blogLikeOrCollectDTO: undefined,
      ...params['blogLikeOrCollectDTO'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /blog/comments */
export async function comments(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.commentsParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBlogCommentVO>('/blog/comments', {
    method: 'GET',
    params: {
      ...params,
      blogCommentDTO: undefined,
      ...params['blogCommentDTO'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /blog/create */
export async function createBlog(body: API.BlogCreateDTO, options?: { [key: string]: any }) {
  return request<API.ResultLong>('/blog/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /blog/del */
export async function delBlog(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delBlogParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultObject>('/blog/del', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /blog/edit */
export async function editBlog(body: API.BlogEditDTO, options?: { [key: string]: any }) {
  return request<API.ResultObject>('/blog/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /blog/likes/toggle */
export async function likeToggle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.likeToggleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBlogLikeOrCollectVO>('/blog/likes/toggle', {
    method: 'GET',
    params: {
      ...params,
      blogLikeOrCollectDTO: undefined,
      ...params['blogLikeOrCollectDTO'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /blog/list */
export async function blogList(body: API.BlogListDTO, options?: { [key: string]: any }) {
  return request<API.ResultPageBlogsVO>('/blog/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
