/**
 * 图片上传组合式函数
 */

import { ref } from 'vue'
import { upload, editByBatch } from '@/api/pictureController'

export interface UploadPreview {
  file: File
  url: string
  tags: string
  id?: number
  status?: 'pending' | 'uploading' | 'uploaded' | 'error'
  errorMsg?: string
}

export function useUpload() {
  const uploadPreviews = ref<UploadPreview[]>([])
  const isUploading = ref(false)

  /**
   * 选择文件并立即上传
   */
  const handleFileSelect = async (event: Event, spaceId: number | null) => {
    const target = event.target as HTMLInputElement
    if (!target.files || !spaceId) {
      console.error('没有选择文件或空间ID为空', { files: target.files, spaceId })
      return
    }

    const files = Array.from(target.files)
    console.log(`开始上传 ${files.length} 个文件到空间 ${spaceId}`)

    // 为每个文件创建预览对象
    const newPreviews: UploadPreview[] = files.map((file) => ({
      file,
      url: URL.createObjectURL(file),
      tags: '',
      status: 'uploading' as const,
    }))

    uploadPreviews.value.push(...newPreviews)
    isUploading.value = true

    // 立即上传每个文件
    for (const preview of newPreviews) {
      try {
        const formData = new FormData()
        formData.append('file', preview.file)

        console.log(`正在上传文件: ${preview.file.name}, 空间ID: ${spaceId}`)
        const res = await upload({ spaceId }, formData)
        console.log('上传响应:', res.data)

        if (res.data && res.data.code === 0 && res.data.data?.id) {
          preview.id = res.data.data.id
          preview.url = res.data.data.thumbUrl || preview.url
          preview.status = 'uploaded'
          console.log(`文件上传成功: ID=${preview.id}, thumbUrl=${preview.url}`)
        } else {
          preview.status = 'error'
          preview.errorMsg = res.data?.message || '上传失败'
          console.error('上传失败:', res.data?.message)
        }
      } catch (err) {
        console.error('上传文件异常:', err)
        preview.status = 'error'
        preview.errorMsg = '上传失败'
      }
    }

    isUploading.value = false
    // 重置input，允许重复选择同一文件
    target.value = ''
  }

  /**
   * 删除预览
   */
  const removePreview = (index: number) => {
    const preview = uploadPreviews.value[index]
    if (preview?.url && preview.url.startsWith('blob:')) {
      URL.revokeObjectURL(preview.url)
    }
    uploadPreviews.value.splice(index, 1)
  }

  /**
   * 批量编辑标签信息（点击"上传X张图片"按钮时调用）
   */
  const handleBatchEdit = async (spaceId: number | null) => {
    if (!spaceId) {
      alert('空间ID不能为空')
      return false
    }

    // 过滤出成功上传的图片
    const uploadedItems = uploadPreviews.value.filter((p) => p.status === 'uploaded' && p.id)

    if (uploadedItems.length === 0) {
      alert('没有可以保存的图片')
      return false
    }

    isUploading.value = true

    try {
      // 构建编辑列表：每个元素包含图片id、标签tags和spaceId
      const editList: API.PictureEditDTO[] = uploadedItems.map((p) => ({
        id: p.id!,
        tags: p.tags || undefined,
        spaceId: spaceId,
      }))

      console.log('批量编辑请求:', editList)
      const res = await editByBatch(editList)
      console.log('批量编辑响应:', res.data)

      if (res.data && res.data.code === 0) {
        alert(`成功保存 ${uploadedItems.length} 张图片的信息！`)
        // 清空预览列表
        uploadPreviews.value = []
        return true
      } else {
        alert(res.data?.message || '保存失败')
        return false
      }
    } catch (err) {
      console.error('批量编辑失败:', err)
      alert('保存信息时出现错误，请重试')
      return false
    } finally {
      isUploading.value = false
    }
  }

  /**
   * 清空预览列表
   */
  const clearPreviews = () => {
    // 释放所有blob URL
    uploadPreviews.value.forEach((preview) => {
      if (preview.url && preview.url.startsWith('blob:')) {
        URL.revokeObjectURL(preview.url)
      }
    })
    uploadPreviews.value = []
  }

  return {
    uploadPreviews,
    isUploading,
    handleFileSelect,
    removePreview,
    handleBatchEdit,
    clearPreviews,
  }
}
