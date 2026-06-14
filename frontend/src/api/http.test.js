import { describe, expect, it, vi } from 'vitest'

const { mockCreate, mockUse } = vi.hoisted(() => {
  const mockUse = vi.fn()
  const mockCreate = vi.fn(() => ({
    interceptors: {
      response: { use: mockUse }
    }
  }))
  return { mockCreate, mockUse }
})

vi.mock('axios', () => ({
  default: {
    create: mockCreate
  }
}))

describe('http client', () => {
  it('creates axios instance with api baseURL and timeout', async () => {
    vi.resetModules()
    await import('./http')

    expect(mockCreate).toHaveBeenCalledWith({
      baseURL: '/api',
      timeout: 10000
    })
  })

  it('response interceptor returns body when code is 200', async () => {
    vi.resetModules()
    await import('./http')

    const onSuccess = mockUse.mock.calls[0][0]
    expect(onSuccess({ data: { code: 200, message: 'success', data: [] } })).toEqual({
      code: 200,
      message: 'success',
      data: []
    })
  })

  it('response interceptor rejects when code is not 200', async () => {
    vi.resetModules()
    await import('./http')

    const onSuccess = mockUse.mock.calls[0][0]
    const response = { data: { code: 400, message: '手机号已注册', data: null } }

    await expect(onSuccess(response)).rejects.toMatchObject({
      message: '手机号已注册',
      code: 400
    })
  })

  it('error interceptor maps network failure message', async () => {
    vi.resetModules()
    await import('./http')

    const onError = mockUse.mock.calls[0][1]

    await expect(onError({ message: 'Network Error' })).rejects.toMatchObject({
      message: '无法连接后端，请确认服务已启动（8080）'
    })
  })
})
