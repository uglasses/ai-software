import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import DashboardView from './DashboardView.vue'

const { mockFetchJobList } = vi.hoisted(() => ({
  mockFetchJobList: vi.fn()
}))

vi.mock('../api/job.js', () => ({
  fetchJobList: mockFetchJobList
}))

const sampleJobs = [
  { id: 1, jobName: 'Java开发', city: '北京', salary: '10000-15000', skillTags: 'Java,Spring' },
  { id: 2, jobName: 'Java后端', city: '上海', salary: '12000-18000', skillTags: 'Java,MySQL' },
  { id: 3, jobName: 'Python开发', city: '北京', salary: '8000-12000', skillTags: 'Python' }
]

describe('DashboardView', () => {
  beforeEach(() => {
    mockFetchJobList.mockReset()
    mockFetchJobList.mockResolvedValue({ code: 200, data: sampleJobs })
  })

  it('computesTotalJobsAndCities', async () => {
    const { wrapper } = mountWithPlugins(DashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('2')
  })

  it('computesTopSkill', async () => {
    const { wrapper } = mountWithPlugins(DashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('Java')
  })

  it('computesAvgSalary', async () => {
    const { wrapper } = mountWithPlugins(DashboardView)
    await flushPromises()

    expect(wrapper.text()).toMatch(/\d+(\.\d+)?K/)
  })
})
