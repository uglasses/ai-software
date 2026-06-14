import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import JobListView from './JobListView.vue'

const { mockFetchJobList } = vi.hoisted(() => ({
  mockFetchJobList: vi.fn()
}))

vi.mock('../api/job.js', () => ({
  fetchJobList: mockFetchJobList
}))

const sampleJobs = [
  { id: 1, jobName: 'Java开发', companyName: 'A公司', city: '北京', salary: '10000-15000', education: '本科', skillTags: 'Java,Spring' },
  { id: 2, jobName: 'Python开发', companyName: 'B公司', city: '上海', salary: '12000-18000', education: '硕士', skillTags: 'Python' },
  { id: 3, jobName: '前端开发', companyName: 'C公司', city: '北京', salary: '9000-14000', education: '本科', skillTags: 'Vue' }
]

describe('JobListView', () => {
  beforeEach(() => {
    mockFetchJobList.mockReset()
    mockFetchJobList.mockResolvedValue({ code: 200, data: sampleJobs })
  })

  it('loadsAndRendersJobs', async () => {
    const { wrapper } = mountWithPlugins(JobListView)
    await flushPromises()

    expect(mockFetchJobList).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Java开发')
    expect(wrapper.text()).toContain('Python开发')
  })

  it('filtersByCityAndKeyword', async () => {
    const { wrapper } = mountWithPlugins(JobListView)
    await flushPromises()

    const keywordInput = wrapper.find('input[placeholder="岗位或技能"]')
    await keywordInput.setValue('Python')
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Python开发')
    expect(wrapper.text()).not.toContain('Java开发')
  })

  it('resetsFilters', async () => {
    const { wrapper } = mountWithPlugins(JobListView)
    await flushPromises()

    const keywordInput = wrapper.find('input[placeholder="岗位或技能"]')
    await keywordInput.setValue('Python')
    const buttons = wrapper.findAll('button')
    const resetBtn = buttons.find((btn) => btn.text().includes('重置'))
    await resetBtn.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Java开发')
    expect(wrapper.text()).toContain('Python开发')
  })

  it('paginatesResults', async () => {
    const manyJobs = Array.from({ length: 25 }, (_, i) => ({
      id: i + 1,
      jobName: `岗位${i + 1}`,
      companyName: '公司',
      city: '北京',
      salary: '10000-15000',
      education: '本科',
      skillTags: 'Java'
    }))
    mockFetchJobList.mockResolvedValue({ code: 200, data: manyJobs })

    const { wrapper } = mountWithPlugins(JobListView)
    await flushPromises()

    expect(wrapper.text()).toContain('岗位1')
    expect(wrapper.text()).not.toContain('岗位25')
  })

  it('handlesApiFailure', async () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    mockFetchJobList.mockResolvedValue({ code: 500, message: 'error' })

    const { wrapper } = mountWithPlugins(JobListView)
    await flushPromises()

    expect(wrapper.text()).toContain('岗位列表')
    errorSpy.mockRestore()
  })
})
