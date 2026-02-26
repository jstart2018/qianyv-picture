// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /picture/${param0} */
export async function getOneById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getOneByIdParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultPictureVO>(`/picture/${param0}`, {
    method: 'GET',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /picture/${param0} */
export async function collectToggle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.collectToggleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.Result>(`/picture/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /picture/checkCollect */
export async function checkCollect(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkCollectParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/picture/checkCollect', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

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

/** 此处后端没有提供注释 POST /picture/download */
export async function pictureDownload(
  body: API.PictureDownLoadDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/picture/download', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
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

/** 此处后端没有提供注释 POST /picture/editByBatch */
export async function editByBatch(body: API.PictureEditDTO[], options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/picture/editByBatch', {
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

/** 此处后端没有提供注释 POST /picture/list/page */
export async function listByPage(body: API.PicturePageQueryDTO, options?: { [key: string]: any }) {
  return request<API.ResultPagePictureListVO>('/picture/list/page', {
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
