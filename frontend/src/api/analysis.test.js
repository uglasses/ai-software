import { describe, expect, it, vi } from 'vitest'

const { mockGet } = vi.hoisted(() => ({
  mockGet: vi.fn()
}))

vi.mock('./http', () => ({
  default: { get: mockGet }
}))

import { fetchCityJobCount } from './analysis'

describe('analysis api', () => {
  it('fetchCityJobCount calls city-job-count endpoint', async () => {
    mockGet.mockResolvedValue({ code: 200, data: [] })

    await fetchCityJobCount()

    expect(mockGet).toHaveBeenCalledWith('/analysis/city-job-count')
  })
})
