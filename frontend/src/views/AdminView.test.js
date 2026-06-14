import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import AdminView from './AdminView.vue'

const {
  mockFetchAdminDashboard,
  mockFetchAdminJobList,
  mockFetchAdminUserList,
  mockFetchAdminLogList
} = vi.hoisted(() => ({
  mockFetchAdminDashboard: vi.fn(),
  mockFetchAdminJobList: vi.fn(),
  mockFetchAdminUserList: vi.fn(),
  mockFetchAdminLogList: vi.fn()
}))

vi.mock('../api/admin', () => ({
  fetchAdminDashboard: mockFetchAdminDashboard,
  fetchAdminJobList: mockFetchAdminJobList,
  addAdminJob: vi.fn(),
  updateAdminJob: vi.fn(),
  deleteAdminJob: vi.fn(),
  toggleJobStatus: vi.fn(),
  importJobs: vi.fn(),
  fetchAdminUserList: mockFetchAdminUserList,
  toggleUserStatus: vi.fn(),
  deleteAdminUser: vi.fn(),
  fetchAdminLogList: mockFetchAdminLogList,
  cleanAdminData: vi.fn()
}))

describe('AdminView', () => {
  beforeEach(() => {
    mockFetchAdminDashboard.mockReset()
    mockFetchAdminJobList.mockReset()
    mockFetchAdminUserList.mockReset()
    mockFetchAdminLogList.mockReset()

    mockFetchAdminDashboard.mockResolvedValue({
      code: 200,
      data: { totalJobs: 100, totalUsers: 50, totalResumes: 30, todayNew: 5 }
    })
    mockFetchAdminJobList.mockResolvedValue({
      code: 200,
      data: {
        total: 2,
        records: [
          { id: 1, jobName: 'Java开发', companyName: 'A公司', city: '北京', status: 1 },
          { id: 2, jobName: 'Python开发', companyName: 'B公司', city: '上海', status: 1 }
        ]
      }
    })
    mockFetchAdminUserList.mockResolvedValue({
      code: 200,
      data: {
        total: 15,
        records: [{ id: 1, username: 'alice', realName: 'Alice', role: 'student', status: 1 }]
      }
    })
    mockFetchAdminLogList.mockResolvedValue({
      code: 200,
      data: {
        total: 3,
        records: [{ id: 1, moduleName: 'resume', operationType: '上传', resultStatus: 1 }]
      }
    })
  })

  it('dashboardTabShowsStats', async () => {
    const { wrapper } = mountWithPlugins(AdminView)
    await flushPromises()

    expect(mockFetchAdminDashboard).toHaveBeenCalled()
    expect(wrapper.text()).toContain('100')
    expect(wrapper.text()).toContain('总岗位数')
  })

  it('jobsTabLoadsAndFilters', async () => {
    const { wrapper } = mountWithPlugins(AdminView)
    await flushPromises()

    const jobsTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('岗位管理'))
    await jobsTab.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Java开发')

    const keywordInput = wrapper.find('input[placeholder="请输入岗位名称"]')
    await keywordInput.setValue('Java')
    const searchBtn = wrapper.findAll('button').find((btn) => btn.text().includes('查询'))
    await searchBtn.trigger('click')
    await flushPromises()

    expect(mockFetchAdminJobList).toHaveBeenCalled()
  })

  it('usersTabPaginates', async () => {
    const { wrapper } = mountWithPlugins(AdminView)
    await flushPromises()

    const usersTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('用户管理'))
    await usersTab.trigger('click')
    await flushPromises()

    expect(mockFetchAdminUserList).toHaveBeenCalled()
    expect(wrapper.text()).toContain('alice')
  })

  it('logsTabRenders', async () => {
    const { wrapper } = mountWithPlugins(AdminView)
    await flushPromises()

    const logsTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('系统日志'))
    await logsTab.trigger('click')
    await flushPromises()

    expect(mockFetchAdminLogList).toHaveBeenCalled()
    expect(wrapper.text()).toContain('resume')
  })

  it('openAddJobDialog', async () => {
    const { wrapper } = mountWithPlugins(AdminView)
    await flushPromises()

    const jobsTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('岗位管理'))
    await jobsTab.trigger('click')
    await flushPromises()

    const addBtn = wrapper.findAll('button').find((btn) => btn.text().includes('新增岗位'))
    await addBtn.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('新增岗位')
    expect(wrapper.find('.el-dialog').exists()).toBe(true)
  })
})
