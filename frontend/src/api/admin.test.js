import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
  mockPut: vi.fn(),
  mockDelete: vi.fn(),
  mockPatch: vi.fn()
}))

vi.mock('./http', () => ({
  default: {
    get: mocks.mockGet,
    post: mocks.mockPost,
    put: mocks.mockPut,
    delete: mocks.mockDelete,
    patch: mocks.mockPatch
  }
}))

import {
  addAdminJob,
  cleanAdminData,
  deleteAdminJob,
  deleteAdminUser,
  fetchAdminDashboard,
  fetchAdminJobList,
  fetchAdminLogList,
  fetchAdminUserList,
  getAdminJob,
  importJobs,
  toggleJobStatus,
  toggleUserStatus,
  updateAdminJob
} from './admin'

describe('admin api', () => {
  beforeEach(() => {
    Object.values(mocks).forEach((fn) => fn.mockReset())
  })

  it('fetchAdminDashboard', async () => {
    await fetchAdminDashboard()
    expect(mocks.mockGet).toHaveBeenCalledWith('/admin/dashboard')
  })

  it('fetchAdminJobList with params', async () => {
    const params = { page: 1, size: 10 }
    await fetchAdminJobList(params)
    expect(mocks.mockGet).toHaveBeenCalledWith('/admin/job-list', { params })
  })

  it('getAdminJob by id', async () => {
    await getAdminJob(5)
    expect(mocks.mockGet).toHaveBeenCalledWith('/admin/job/5')
  })

  it('addAdminJob posts body', async () => {
    const data = { jobName: '测试岗' }
    await addAdminJob(data)
    expect(mocks.mockPost).toHaveBeenCalledWith('/admin/job', data)
  })

  it('updateAdminJob puts by id', async () => {
    const data = { jobName: '更新' }
    await updateAdminJob(3, data)
    expect(mocks.mockPut).toHaveBeenCalledWith('/admin/job/3', data)
  })

  it('deleteAdminJob', async () => {
    await deleteAdminJob(8)
    expect(mocks.mockDelete).toHaveBeenCalledWith('/admin/job/8')
  })

  it('toggleJobStatus patches status', async () => {
    await toggleJobStatus(2, 1)
    expect(mocks.mockPatch).toHaveBeenCalledWith('/admin/job/2/status', { status: 1 })
  })

  it('importJobs posts multipart', async () => {
    const file = new File(['csv'], 'jobs.csv', { type: 'text/csv' })
    await importJobs(file)
    expect(mocks.mockPost).toHaveBeenCalledWith(
      '/admin/job/import',
      expect.any(FormData),
      { headers: { 'Content-Type': 'multipart/form-data' } }
    )
  })

  it('fetchAdminUserList', async () => {
    await fetchAdminUserList({ keyword: 'a' })
    expect(mocks.mockGet).toHaveBeenCalledWith('/admin/user-list', { params: { keyword: 'a' } })
  })

  it('toggleUserStatus', async () => {
    await toggleUserStatus(4, 0)
    expect(mocks.mockPatch).toHaveBeenCalledWith('/admin/user/4/status', { status: 0 })
  })

  it('deleteAdminUser', async () => {
    await deleteAdminUser(6)
    expect(mocks.mockDelete).toHaveBeenCalledWith('/admin/user/6')
  })

  it('fetchAdminLogList', async () => {
    await fetchAdminLogList({ page: 1 })
    expect(mocks.mockGet).toHaveBeenCalledWith('/admin/log-list', { params: { page: 1 } })
  })

  it('cleanAdminData', async () => {
    await cleanAdminData('logs')
    expect(mocks.mockPost).toHaveBeenCalledWith('/admin/clean-data', { type: 'logs' })
  })
})
