// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /user/${param0} */
export async function getById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getByIdParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultUserInfoVO>(`/user/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /user/${param0} */
export async function update(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateParams,
  body: API.UserEditDTO,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<boolean>(`/user/${param0}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /user/${param0} */
export async function delete1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.delete1Params,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<boolean>(`/user/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/avatar */
export async function updateAvatar(body: {}, options?: { [key: string]: any }) {
  return request<API.ResultString>('/user/avatar', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/changeRole */
export async function changeRole(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.changeRoleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/changeRole', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/changeStatus */
export async function changeStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.changeStatusParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/changeStatus', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/checkFollow */
export async function checkFollow(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkFollowParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>('/user/checkFollow', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/code */
export async function sendCode(body: API.SendCodeDTO, options?: { [key: string]: any }) {
  return request<API.ResultString>('/user/code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/count */
export async function count(options?: { [key: string]: any }) {
  return request<API.ResultLong>('/user/count', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/createUser */
export async function createUser(
  body: API.UserLoginByPasswordDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/createUser', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/followToggle */
export async function followToggle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.followToggleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultObject>('/user/followToggle', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/getHotUser */
export async function getHotUser(options?: { [key: string]: any }) {
  return request<API.ResultListUserInfoVO>('/user/getHotUser', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/login/code */
export async function loginWithCode(
  body: API.UserLoginByCodeDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/login/code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/login/password */
export async function loginWithPassword(
  body: API.UserLoginByPasswordDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/login/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/loginUser */
export async function getLoginUser(options?: { [key: string]: any }) {
  return request<API.ResultUserInfoVO>('/user/loginUser', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.ResultString>('/user/logout', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/page */
export async function page(body: API.UserQueryDTO, options?: { [key: string]: any }) {
  return request<API.ResultPageUser>('/user/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/password */
export async function changePassword(
  body: API.ChangePasswordDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/password/code */
export async function sendPasswordCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendPasswordCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>('/user/password/code', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
