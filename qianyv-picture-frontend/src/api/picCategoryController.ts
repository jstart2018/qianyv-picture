// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /picCategory/add */
export async function add(body: API.PicCategoryDTO, options?: { [key: string]: any }) {
  return request<API.ResultPicCategoryVO>('/picCategory/add', {
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

/** 此处后端没有提供注释 GET /picCategory/listAll */
export async function listAll(options?: { [key: string]: any }) {
  return request<API.ResultListPicCategoryVO>('/picCategory/listAll', {
    method: 'GET',
    ...(options || {}),
  })
}
