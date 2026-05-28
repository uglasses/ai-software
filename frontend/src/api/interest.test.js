import { beforeEach, describe, expect, it, vi } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

vi.mock('./http', () => ({
  default: { get: mockGet, post: mockPost }
}))

import { fetchInterestJobs, saveInterestJobs } from './interest'

describe('interest api', () => {
  beforeEach(() => {
    mockGet.mockReset()
    mockPost.mockReset()
  })

  it('saveInterestJobs posts payload', async () => {
    const payload = { userId: 1, jobs: [{ jobName: 'Java开发', priority: 3 }] }
    await saveInterestJobs(payload)
    expect(mockPost).toHaveBeenCalledWith('/user/interest-jobs', payload)
  })

  it('fetchInterestJobs passes userId', async () => {
    await fetchInterestJobs(9)
    expect(mockGet).toHaveBeenCalledWith('/user/interest-jobs', { params: { userId: 9 } })
  })
})
