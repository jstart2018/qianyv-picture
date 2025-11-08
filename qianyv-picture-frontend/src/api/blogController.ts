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

/** 此处后端没有提供注释 POST /blog/add */
export async function addBlog(body: API.BlogCreateDTO, options?: { [key: string]: any }) {
  return request<API.ResultLong>('/blog/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /blog/blogCollect */
export async function collectToggle1(
  body: API.BlogLikeOrCollectDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultObject>('/blog/blogCollect', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /blog/blogLikes */
export async function likesToggle(
  body: API.BlogLikeOrCollectDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultObject>('/blog/blogLikes', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /blog/checkCollect */
export async function checkCollect1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkCollect1Params,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/blog/checkCollect', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /blog/checkLike */
export async function checkBlogLike(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkBlogLikeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/blog/checkLike', {
    method: 'GET',
    params: {
      ...params,
    },
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
