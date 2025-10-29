// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /space/create */
export async function createSpace(options?: { [key: string]: any }) {
  return request<API.ResultLong>('/space/create', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/edit */
export async function editSpace(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.editSpaceParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/space/edit', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/editRole */
export async function editUserRole(body: API.SpaceUserEditDTO, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/space/editRole', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /space/get */
export async function getSpaceInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceInfoParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultObject>('/space/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/kickout */
export async function kickOutMember(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.kickOutMemberParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/space/kickout', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/list */
export async function listSpaceUsers(
  body: API.SpaceUserQueryDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageSpaceUserVO>('/space/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /space/upgrade */
export async function upgradeSpace(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.upgradeSpaceParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/space/upgrade', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
