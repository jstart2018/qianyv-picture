// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /picCategory/ */
export async function listAll(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAllParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultListString>('/picCategory/', {
    method: 'GET',
    params: {
      ...params,
      picCategoryDTO: undefined,
      ...params['picCategoryDTO'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picCategory/add */
export async function add(body: API.PicCategoryDTO, options?: { [key: string]: any }) {
  return request<API.ResultString>('/picCategory/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picCategory/delete */
export async function deleteUsingPost(body: API.PicCategoryDTO, options?: { [key: string]: any }) {
  return request<API.ResultObject>('/picCategory/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
