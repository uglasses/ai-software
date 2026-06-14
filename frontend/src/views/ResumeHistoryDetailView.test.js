import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import ResumeHistoryDetailView from './ResumeHistoryDetailView.vue'

const {
  mockSuccess,
  mockError,
  mockFetchResumeHistoryDetail,
  mockGenerateJobSelectionAdvice,
  mockGenerateInterestResumeAdvice
} = vi.hoisted(() => ({
  mockSuccess: vi.fn(),
  mockError: vi.fn(),
  mockFetchResumeHistoryDetail: vi.fn(),
  mockGenerateJobSelectionAdvice: vi.fn(),
  mockGenerateInterestResumeAdvice: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '15' } })
}))

vi.mock('@/api/resume', () => ({
  fetchResumeHistoryDetail: mockFetchResumeHistoryDetail,
  generateJobSelectionAdvice: mockGenerateJobSelectionAdvice,
  generateInterestResumeAdvice: mockGenerateInterestResumeAdvice
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: {
      warning: vi.fn(),
      success: mockSuccess,
      error: mockError
    }
  }
})

const detailData = {
  resumeId: 15,
  resumeName: '我的简历',
  targetJobName: 'Java开发',
  fileType: 'pdf',
  parseStatus: 2,
  resumeText: '熟悉 Spring Boot',
  skills: [{ id: 1, skillName: 'Java' }],
  parseResult: { suggestions: '补充项目经历', parsedMajor: '计算机', parsedSchool: '测试大学', parsedEducation: '本科' },
  matches: [{ jobName: '后端开发', companyName: 'A公司', totalScore: 90 }],
  jobSelectionAdvice: '',
  jobSelectionAdviceModel: '',
  interestResumeAdvice: '',
  interestResumeAdviceModel: ''
}

describe('ResumeHistoryDetailView', () => {
  beforeEach(() => {
    mockSuccess.mockReset()
    mockError.mockReset()
    mockFetchResumeHistoryDetail.mockReset()
    mockGenerateJobSelectionAdvice.mockReset()
    mockGenerateInterestResumeAdvice.mockReset()
    mockFetchResumeHistoryDetail.mockResolvedValue({ code: 200, data: detailData })
  })

  it('loadsDetailOnMount', async () => {
    const { wrapper } = mountWithPlugins(ResumeHistoryDetailView)
    await flushPromises()

    expect(mockFetchResumeHistoryDetail).toHaveBeenCalledWith('15')
    expect(wrapper.text()).toContain('我的简历')
    expect(wrapper.text()).toContain('Java')
    expect(wrapper.text()).toContain('后端开发')
  })

  it('generateJobAdviceSuccess', async () => {
    mockGenerateJobSelectionAdvice.mockResolvedValue({
      code: 200,
      data: { advice: '建议投递 Java 岗位', model: 'deepseek-chat' }
    })

    const { wrapper } = mountWithPlugins(ResumeHistoryDetailView)
    await flushPromises()

    const adviceBtn = wrapper.findAll('button').find((btn) => btn.text().includes('生成岗位选择建议'))
    await adviceBtn.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('建议投递 Java 岗位')
    expect(mockSuccess).toHaveBeenCalledWith('岗位选择建议已生成')
  })

  it('generateJobAdviceFailure', async () => {
    mockGenerateJobSelectionAdvice.mockResolvedValue({ code: 503, message: '服务不可用' })

    const { wrapper } = mountWithPlugins(ResumeHistoryDetailView)
    await flushPromises()

    const adviceBtn = wrapper.findAll('button').find((btn) => btn.text().includes('生成岗位选择建议'))
    await adviceBtn.trigger('click')
    await flushPromises()

    expect(mockError).toHaveBeenCalledWith('服务不可用')
  })

  it('generateInterestAdviceSuccess', async () => {
    mockGenerateInterestResumeAdvice.mockResolvedValue({
      code: 200,
      data: { advice: '突出 SQL 技能', model: 'deepseek-chat' }
    })

    const { wrapper } = mountWithPlugins(ResumeHistoryDetailView)
    await flushPromises()

    const adviceBtn = wrapper.findAll('button').find((btn) => btn.text().includes('按兴趣岗位生成简历修改建议'))
    await adviceBtn.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('突出 SQL 技能')
    expect(mockSuccess).toHaveBeenCalledWith('兴趣岗位简历建议已生成')
  })

  it('showsEmptyAdvicePlaceholder', async () => {
    const { wrapper } = mountWithPlugins(ResumeHistoryDetailView)
    await flushPromises()

    expect(wrapper.text()).toContain('暂无建议，点击上方按钮生成')
  })
})
