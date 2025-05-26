import request from '@/utils/request'
import type { Page } from '@/types/common'

export interface AdminHelpInfoPageParams {
    page?: number
    size?: number
    keyword?: string
    type?: string
    status?: string
}

export interface HelpInfoVO {
    infoId: number
    title: string
    description: string
    type: string
    status: string
    publisherId: number
    publisherName: string
    publisherAvatar: string
    createdAt: string
    updatedAt: string
    deadline: string
    viewCount: number
    applicationCount: number
}

/**
 * 管理员分页查询互助任务
 */
export const adminPageHelpInfo = (params: AdminHelpInfoPageParams) => {
    return request.get<Page<HelpInfoVO>>('/api/admin/helpinfo/page', { params })
}

/**
 * 管理员更新互助任务状态
 */
export const adminUpdateHelpInfoStatus = (id: number, status: string) => {
    return request.patch(`/api/admin/helpinfo/${id}/status`, null, {
        params: { status }
    })
}

/**
 * 管理员删除互助任务
 */
export const adminDeleteHelpInfo = (id: number) => {
    return request.delete(`/api/admin/helpinfo/${id}`)
}
