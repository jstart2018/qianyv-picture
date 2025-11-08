// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /common/add */
export async function addCommon(body: API.CommentAddDTO, options?: { [key: string]: any }) {
  return request<API.Result>('/common/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /common/listParentCommon */
export async function listParentCommon(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listParentCommonParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultListCommentQueryVO>('/common/listParentCommon', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /common/remove */
export async function removeCommon(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.removeCommonParams,
  options?: { [key: string]: any }
) {
  return request<API.Result>('/common/remove', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
