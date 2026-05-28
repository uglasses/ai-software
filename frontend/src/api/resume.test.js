import { beforeEach, describe, expect, it, vi } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

vi.mock('./http', () => ({
  default: {
    get: mockGet,
    post: mockPost
  }
}))

import {
  createResume,
  fetchResumeHistory,
  fetchResumeHistoryDetail,
  fetchResumeMatches,
  generateInterestResumeAdvice,
  generateJobSelectionAdvice,
  triggerResumeMatch,
  uploadResume
} from './resume'

describe('resume api', () => {
  beforeEach(() => {
    mockGet.mockReset()
    mockPost.mockReset()
  })

  it('createResume posts payload to /resume', async () => {
    const payload = { userId: 1, resumeName: 'a.pdf' }
    mockPost.mockResolvedValue({ code: 200 })

    await createResume(payload)

    expect(mockPost).toHaveBeenCalledWith('/resume', payload)
  })

  it('uploadResume uses multipart headers and 12s timeout', async () => {
    const formData = new FormData()
    mockPost.mockResolvedValue({ code: 200 })

    await uploadResume(formData)

    expect(mockPost).toHaveBeenCalledWith('/resume/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 12000
    })
  })

  it('triggerResumeMatch posts with topN param and 12s timeout', async () => {
    mockPost.mockResolvedValue({ code: 200 })

    await triggerResumeMatch(10, 5)

    expect(mockPost).toHaveBeenCalledWith('/resume/10/match', null, {
      params: { topN: 5 },
      timeout: 12000
    })
  })

  it('triggerResumeMatch defaults topN to 20', async () => {
    await triggerResumeMatch(3)

    expect(mockPost).toHaveBeenCalledWith(
      '/resume/3/match',
      null,
      expect.objectContaining({ params: { topN: 20 } })
    )
  })

  it('fetchResumeMatches gets by resume id', async () => {
    await fetchResumeMatches(7)

    expect(mockGet).toHaveBeenCalledWith('/resume/7/matches')
  })

  it('fetchResumeHistory passes userId query', async () => {
    await fetchResumeHistory(42)

    expect(mockGet).toHaveBeenCalledWith('/resume/history', { params: { userId: 42 } })
  })

  it('fetchResumeHistoryDetail gets detail endpoint', async () => {
    await fetchResumeHistoryDetail(99)

    expect(mockGet).toHaveBeenCalledWith('/resume/99')
  })

  it('generateJobSelectionAdvice uses 120s timeout', async () => {
    await generateJobSelectionAdvice(1)

    expect(mockPost).toHaveBeenCalledWith('/resume/1/job-selection-advice', null, {
      timeout: 120000
    })
  })

  it('generateInterestResumeAdvice uses 120s timeout', async () => {
    await generateInterestResumeAdvice(2)

    expect(mockPost).toHaveBeenCalledWith('/resume/2/interest-resume-advice', null, {
      timeout: 120000
    })
  })
})
