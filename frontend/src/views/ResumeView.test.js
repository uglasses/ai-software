import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import { useUserStore } from '@/stores/user'
import ResumeView from './ResumeView.vue'

const {
  mockPush,
  mockWarning,
  mockSuccess,
  mockError,
  mockFetchInterestJobs,
  mockSaveInterestJobs,
  mockUploadResume,
  mockTriggerResumeMatch,
  mockFetchResumeMatches
} = vi.hoisted(() => ({
  mockPush: vi.fn(),
  mockWarning: vi.fn(),
  mockSuccess: vi.fn(),
  mockError: vi.fn(),
  mockFetchInterestJobs: vi.fn(),
  mockSaveInterestJobs: vi.fn(),
  mockUploadResume: vi.fn(),
  mockTriggerResumeMatch: vi.fn(),
  mockFetchResumeMatches: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

vi.mock('@/api/interest', () => ({
  fetchInterestJobs: mockFetchInterestJobs,
  saveInterestJobs: mockSaveInterestJobs
}))

vi.mock('@/api/resume', () => ({
  uploadResume: mockUploadResume,
  triggerResumeMatch: mockTriggerResumeMatch,
  fetchResumeMatches: mockFetchResumeMatches
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: {
      warning: mockWarning,
      success: mockSuccess,
      error: mockError
    }
  }
})

describe('ResumeView', () => {
  beforeEach(() => {
    localStorage.clear()
    mockPush.mockReset()
    mockWarning.mockReset()
    mockSuccess.mockReset()
    mockFetchInterestJobs.mockReset()
    mockSaveInterestJobs.mockReset()
    mockUploadResume.mockReset()
    mockTriggerResumeMatch.mockReset()
    mockFetchResumeMatches.mockReset()
    mockFetchInterestJobs.mockResolvedValue({ data: [{ jobName: 'Java开发', priority: 1 }] })
  })

  it('loadsInterestOnMount', async () => {
    mountWithPlugins(ResumeView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 7, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    expect(mockFetchInterestJobs).toHaveBeenCalledWith(7)
  })

  it('saveInterestRequiresLogin', async () => {
    const absorbNoLogin = (event) => {
      if (event?.reason?.message === 'NO_LOGIN') {
        event.preventDefault()
      }
    }
    process.on('unhandledRejection', absorbNoLogin)

    try {
      const { wrapper } = mountWithPlugins(ResumeView)
      await flushPromises()

      const saveBtn = wrapper.findAll('button').find((btn) => btn.text().includes('保存兴趣岗位'))
      await saveBtn.trigger('click')
      await flushPromises()

      expect(mockWarning).toHaveBeenCalledWith('请先去登录页注册/登录')
    } finally {
      process.off('unhandledRejection', absorbNoLogin)
    }
  })

  it('saveInterestWithEmptyList', async () => {
    const { wrapper } = mountWithPlugins(ResumeView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 7, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    const interestInput = wrapper.find('input[placeholder*="数据分析师"]')
    await interestInput.setValue('   ')
    const saveBtn = wrapper.findAll('button').find((btn) => btn.text().includes('保存兴趣岗位'))
    await saveBtn.trigger('click')

    expect(mockWarning).toHaveBeenCalledWith('请至少填写一个兴趣岗位')
  })

  it('uploadRequiresFile', async () => {
    const { wrapper } = mountWithPlugins(ResumeView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 7, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    const uploadBtn = wrapper.findAll('button').find((btn) => btn.text().includes('上传并进入历史详情'))
    await uploadBtn.trigger('click')

    expect(mockWarning).toHaveBeenCalledWith('请先选择简历文件')
  })

  it('uploadRequiresResumeName', async () => {
    const { wrapper } = mountWithPlugins(ResumeView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 7, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    const resumeNameInput = wrapper.find('input[placeholder*="数据分析师简历"]')
    await resumeNameInput.setValue('  ')
    const uploadBtn = wrapper.findAll('button').find((btn) => btn.text().includes('上传并进入历史详情'))
    await uploadBtn.trigger('click')

    expect(mockWarning).toHaveBeenCalledWith('请填写简历名称')
  })

  it('uploadAndMatchSuccess', async () => {
    mockUploadResume.mockResolvedValue({ code: 200, data: { resumeId: 99 } })
    mockTriggerResumeMatch.mockResolvedValue({ code: 200 })
    mockFetchResumeMatches.mockResolvedValue({
      code: 200,
      data: [{ jobName: 'Java开发', companyName: 'A公司', city: '北京', totalScore: 88 }]
    })

    const { wrapper } = mountWithPlugins(ResumeView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 7, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    const file = new File(['pdf'], 'resume.pdf', { type: 'application/pdf' })
    const uploadComponent = wrapper.findComponent({ name: 'ElUpload' })
    uploadComponent.props('onChange')({ raw: file })

    const uploadBtn = wrapper.findAll('button').find((btn) => btn.text().includes('上传并进入历史详情'))
    await uploadBtn.trigger('click')
    await flushPromises()

    expect(mockUploadResume).toHaveBeenCalled()
    expect(mockTriggerResumeMatch).toHaveBeenCalledWith(99, 20)
    expect(mockPush).toHaveBeenCalledWith('/resume/history/99')
    expect(mockSuccess).toHaveBeenCalledWith('匹配完成，返回 1 条结果')
  })
})
