import { beforeEach, describe, expect, it, vi } from 'vitest'

const { mockGet } = vi.hoisted(() => ({
  mockGet: vi.fn()
}))

vi.mock('./http', () => ({
  default: {
    get: mockGet
  }
}))

import { fetchJobList } from './job'

describe('job api', () => {
  beforeEach(() => {
    mockGet.mockReset()
  })

  it('fetchJobList calls /job/list with params', async () => {
    const params = { city: '北京', page: 1 }
    mockGet.mockResolvedValue({ code: 200, data: [] })

    await fetchJobList(params)

    expect(mockGet).toHaveBeenCalledWith('/job/list', { params })
  })

  it('fetchJobList defaults to empty params', async () => {
    await fetchJobList()

    expect(mockGet).toHaveBeenCalledWith('/job/list', { params: {} })
  })
})
