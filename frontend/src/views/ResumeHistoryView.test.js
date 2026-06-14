import { nextTick } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import { useUserStore } from '@/stores/user'
import ResumeHistoryView from './ResumeHistoryView.vue'

const { mockPush, mockWarning, mockError, mockFetchResumeHistory } = vi.hoisted(() => ({
  mockPush: vi.fn(),
  mockWarning: vi.fn(),
  mockError: vi.fn(),
  mockFetchResumeHistory: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

vi.mock('@/api/resume', () => ({
  fetchResumeHistory: mockFetchResumeHistory
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: {
      warning: mockWarning,
      success: vi.fn(),
      error: mockError
    }
  }
})

describe('ResumeHistoryView', () => {
  beforeEach(() => {
    localStorage.clear()
    mockPush.mockReset()
    mockWarning.mockReset()
    mockError.mockReset()
    mockFetchResumeHistory.mockReset()
    mockFetchResumeHistory.mockResolvedValue({
      data: [
        {
          resumeId: 10,
          resumeName: '测试简历',
          fileType: 'pdf',
          targetJobName: 'Java开发',
          parseStatus: 2,
          resumeTextPreview: '预览文本',
          createdAt: '2026-06-11T10:30:00'
        }
      ]
    })
  })

  it('redirectsWhenNotLoggedIn', async () => {
    mountWithPlugins(ResumeHistoryView)
    await flushPromises()

    expect(mockWarning).toHaveBeenCalledWith('请先登录后查看历史记录')
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('loadsHistoryForUser', async () => {
    const { wrapper } = mountWithPlugins(ResumeHistoryView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 5, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    expect(mockFetchResumeHistory).toHaveBeenCalledWith(5)
    expect(wrapper.text()).toContain('测试简历')
  })

  it('formatsDate', async () => {
    const { wrapper } = mountWithPlugins(ResumeHistoryView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 5, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()
    await nextTick()

    expect(wrapper.text()).toContain('测试简历')
    expect(wrapper.find('.el-table__body').text()).toContain('2026-06-11 10:30:00')
  })

  it('goDetailNavigates', async () => {
    const { wrapper } = mountWithPlugins(ResumeHistoryView, {
      configurePinia: (pinia) => {
        useUserStore(pinia).setUser({ userId: 5, username: 'u', phone: '1', email: 'e@e.com' })
      }
    })
    await flushPromises()

    const detailBtn = wrapper.findAll('button').find((btn) => btn.text().includes('查看详情'))
    await detailBtn.trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/resume/history/10')
  })
})
