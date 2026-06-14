import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import AnalysisView from './AnalysisView.vue'

const { mockDispose, mockSetOption, mockInit, mockFetchJobList } = vi.hoisted(() => {
  const mockDispose = vi.fn()
  const mockSetOption = vi.fn()
  const mockInit = vi.fn(() => ({
    setOption: mockSetOption,
    resize: vi.fn(),
    dispose: mockDispose
  }))
  return {
    mockDispose,
    mockSetOption,
    mockInit,
    mockFetchJobList: vi.fn()
  }
})

vi.mock('echarts', () => ({
  init: mockInit
}))

vi.mock('../api/job.js', () => ({
  fetchJobList: mockFetchJobList
}))

const sampleJobs = [
  { id: 1, jobName: 'Java开发', city: '北京', salary: '10000-15000', skillTags: 'Java,Spring' },
  { id: 2, jobName: 'Python开发', city: '上海', salary: '12000-18000', skillTags: 'Python' }
]

describe('AnalysisView', () => {
  beforeEach(() => {
    mockInit.mockClear()
    mockDispose.mockClear()
    mockSetOption.mockClear()
    mockFetchJobList.mockReset()
    mockFetchJobList.mockResolvedValue({ code: 200, data: sampleJobs })
  })

  it('loadsMetricsFromJobs', async () => {
    const { wrapper } = mountWithPlugins(AnalysisView)
    await flushPromises()

    expect(wrapper.text()).toContain('平均薪资')
    expect(wrapper.text()).toContain('热门技能')
    expect(wrapper.text()).toContain('Java')
  })

  it('initializesChart', async () => {
    mountWithPlugins(AnalysisView)
    await flushPromises()

    expect(mockInit).toHaveBeenCalled()
    expect(mockSetOption).toHaveBeenCalled()
  })

  it('handlesEmptyJobs', async () => {
    mockFetchJobList.mockResolvedValue({ code: 200, data: [] })

    const { wrapper } = mountWithPlugins(AnalysisView)
    await flushPromises()

    expect(wrapper.text()).toContain('平均薪资')
    expect(wrapper.text()).toContain('-')
  })

  it('disposesChartOnUnmount', async () => {
    const { wrapper } = mountWithPlugins(AnalysisView)
    await flushPromises()

    wrapper.unmount()

    expect(mockDispose).toHaveBeenCalled()
  })
})
