import { beforeEach, describe, expect, it, vi } from 'vitest'

const { mockPost } = vi.hoisted(() => ({
  mockPost: vi.fn()
}))

vi.mock('./http', () => ({
  default: {
    post: mockPost
  }
}))

import { login, register } from './auth'

describe('auth api', () => {
  beforeEach(() => {
    mockPost.mockReset()
  })

  it('register posts to /auth/register', async () => {
    const payload = { phone: '13900000000', password: 'secret12' }
    mockPost.mockResolvedValue({ code: 200, data: { userId: 1 } })

    await register(payload)

    expect(mockPost).toHaveBeenCalledWith('/auth/register', payload)
  })

  it('login posts to /auth/login', async () => {
    const payload = { identifier: 'user', password: 'secret12' }
    mockPost.mockResolvedValue({ code: 200, data: { username: 'user' } })

    await login(payload)

    expect(mockPost).toHaveBeenCalledWith('/auth/login', payload)
  })
})
