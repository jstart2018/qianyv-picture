// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /app/chat/${param0} */
export async function doChat(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doChatParams,
  options?: { [key: string]: any }
) {
  const { conversationId: param0, ...queryParams } = params
  return request<API.ServerSentEventString[]>(`/app/chat/${param0}`, {
    method: 'POST',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/chat/history */
export async function getChatHistory(
  body: API.ChatMemoryQueryDTO,
  options?: { [key: string]: any }
) {
  return request<API.ResultListChatMemoryVO>('/app/chat/history', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/create/session */
export async function createSession(options?: { [key: string]: any }) {
  return request<API.ResultLong>('/app/create/session', {
    method: 'GET',
    ...(options || {}),
  })
}
