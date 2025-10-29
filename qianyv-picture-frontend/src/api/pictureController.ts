// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 DELETE /picture/delete */
export async function delete2(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete2Params,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/picture/delete', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/edit */
export async function edit(body: API.PictureEditDTO, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/list */
export async function list(body: API.PictureQueryListDTO, options?: { [key: string]: any }) {
  return request<API.ResultPagePictureListVO>('/picture/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/uploadByFile */
export async function upload(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.ResultPictureUploadVO>('/picture/uploadByFile', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/uploadByUrl */
export async function uploadByUrl(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadByUrlParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultPictureUploadVO>('/picture/uploadByUrl', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
