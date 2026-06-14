import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import { useUserStore } from '@/stores/user'
import LoginView from './LoginView.vue'

const { mockPush, mockWarning, mockSuccess, mockError, mockLogin, mockRegister } = vi.hoisted(() => ({
  mockPush: vi.fn(),
  mockWarning: vi.fn(),
  mockSuccess: vi.fn(),
  mockError: vi.fn(),
  mockLogin: vi.fn(),
  mockRegister: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

vi.mock('@/api/auth', () => ({
  login: mockLogin,
  register: mockRegister
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

describe('LoginView', () => {
  beforeEach(() => {
    localStorage.clear()
    mockPush.mockReset()
    mockWarning.mockReset()
    mockSuccess.mockReset()
    mockError.mockReset()
    mockLogin.mockReset()
    mockRegister.mockReset()
  })

  it('warnsOnEmptySubmit', async () => {
    const { wrapper } = mountWithPlugins(LoginView)
    await wrapper.find('button.full-button').trigger('click')

    expect(mockWarning).toHaveBeenCalledWith('请先填写手机号/邮箱和密码')
    expect(mockLogin).not.toHaveBeenCalled()
  })

  it('loginSuccessNavigates', async () => {
    mockLogin.mockResolvedValue({
      data: { userId: 1, username: 'alice', phone: '13800000000', email: 'a@example.com' }
    })

    const { wrapper, pinia } = mountWithPlugins(LoginView)
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13800000000')
    await inputs[1].setValue('password123')
    await wrapper.find('button.full-button').trigger('click')
    await flushPromises()

    const store = useUserStore(pinia)
    expect(store.userId).toBe(1)
    expect(mockSuccess).toHaveBeenCalledWith('登录成功')
    expect(mockPush).toHaveBeenCalledWith('/resume')
  })

  it('registerUsesEmailPayload', async () => {
    mockRegister.mockResolvedValue({
      data: { userId: 2, username: 'bob', phone: null, email: 'bob@example.com' }
    })

    const { wrapper } = mountWithPlugins(LoginView)
    const registerTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('注册'))
    await registerTab.trigger('click')

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('bob@example.com')
    await inputs[1].setValue('password123')
    await wrapper.find('button.full-button').trigger('click')
    await flushPromises()

    expect(mockRegister).toHaveBeenCalledWith(
      expect.objectContaining({ email: 'bob@example.com', phone: null })
    )
  })

  it('registerUsesPhonePayload', async () => {
    mockRegister.mockResolvedValue({
      data: { userId: 3, username: 'carol', phone: '13900000001', email: null }
    })

    const { wrapper } = mountWithPlugins(LoginView)
    const registerTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('注册'))
    await registerTab.trigger('click')

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13900000001')
    await inputs[1].setValue('password123')
    await wrapper.find('button.full-button').trigger('click')
    await flushPromises()

    expect(mockRegister).toHaveBeenCalledWith(
      expect.objectContaining({ phone: '13900000001', email: null })
    )
  })

  it('showsErrorOnLoginFailure', async () => {
    mockLogin.mockRejectedValue(new Error('账号或密码错误'))

    const { wrapper } = mountWithPlugins(LoginView)
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13800000000')
    await inputs[1].setValue('wrong')
    await wrapper.find('button.full-button').trigger('click')
    await flushPromises()

    expect(mockError).toHaveBeenCalledWith('账号或密码错误')
  })

  it('warnsWhenRegisterPasswordTooShort', async () => {
    const { wrapper } = mountWithPlugins(LoginView)
    const registerTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('注册'))
    await registerTab.trigger('click')

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13900000002')
    await inputs[1].setValue('12345')
    await wrapper.find('button.full-button').trigger('click')

    expect(mockWarning).toHaveBeenCalledWith('密码长度需在 6～64 位')
    expect(mockRegister).not.toHaveBeenCalled()
  })

  it('showsRegisterErrorOnApiFailure', async () => {
    mockRegister.mockRejectedValue(new Error('手机号已注册'))

    const { wrapper } = mountWithPlugins(LoginView)
    const registerTab = wrapper.findAll('.el-tabs__item').find((tab) => tab.text().includes('注册'))
    await registerTab.trigger('click')

    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('13900000002')
    await inputs[1].setValue('password123')
    await wrapper.find('button.full-button').trigger('click')
    await flushPromises()

    expect(mockError).toHaveBeenCalledWith('手机号已注册')
    expect(mockPush).not.toHaveBeenCalled()
  })
})
