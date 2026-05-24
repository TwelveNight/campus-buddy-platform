export const ACCOUNT_DISABLED_PATH = '/account-disabled'

export type AccountUnavailableStatus = 'BANNED' | 'INACTIVE' | 'UNAVAILABLE'

export interface AccountUnavailableInfo {
  status: AccountUnavailableStatus
  message: string
  updatedAt: number
}

const STORAGE_KEY = 'accountUnavailable'

export const isUnavailableStatus = (status?: string | null) => {
  const normalized = (status || '').toUpperCase()
  return normalized === 'BANNED' || normalized === 'INACTIVE'
}

export const isAccountUnavailableMessage = (message?: string | null) => {
  if (!message) return false
  return message.includes('账号被禁用') ||
    message.includes('账号已被禁用') ||
    message.includes('账号未激活') ||
    message.includes('账号被禁用或不可用') ||
    message.includes('不可用')
}

export const getAccountUnavailableMessage = (
  status: AccountUnavailableStatus = 'UNAVAILABLE',
  fallback?: string
) => {
  if (fallback) return fallback
  if (status === 'BANNED') return '账号已被禁用，请联系管理员处理。'
  if (status === 'INACTIVE') return '账号未激活，请联系管理员处理。'
  return '账号当前不可用，请联系管理员处理。'
}

export const inferAccountUnavailableStatus = (message?: string | null): AccountUnavailableStatus => {
  if (message?.includes('未激活')) return 'INACTIVE'
  if (message?.includes('禁用')) return 'BANNED'
  return 'UNAVAILABLE'
}

export const saveAccountUnavailable = (
  status: AccountUnavailableStatus = 'UNAVAILABLE',
  message?: string
): AccountUnavailableInfo => {
  const info = {
    status,
    message: getAccountUnavailableMessage(status, message),
    updatedAt: Date.now()
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(info))
  return info
}

export const getAccountUnavailable = (): AccountUnavailableInfo | null => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const clearAccountUnavailable = () => {
  localStorage.removeItem(STORAGE_KEY)
}
