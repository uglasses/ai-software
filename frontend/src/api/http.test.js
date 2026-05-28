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

  it('response interceptor returns response.data', async () => {
    vi.resetModules()
    await import('./http')

    const unwrap = mockUse.mock.calls[0][0]
    expect(unwrap({ data: { code: 200, message: 'success', data: [] } })).toEqual({
      code: 200,
      message: 'success',
      data: []
    })
  })
})
