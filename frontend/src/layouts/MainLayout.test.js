import { nextTick } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import { useUserStore } from '@/stores/user'
import MainLayout from './MainLayout.vue'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/dashboard', meta: { title: '首页看板' } }),
  useRouter: () => ({ push: mockPush })
}))

describe('MainLayout', () => {
  beforeEach(() => {
    localStorage.clear()
    mockPush.mockReset()
  })

  it('showsLoginButtonWhenGuest', () => {
    const { wrapper } = mountWithPlugins(MainLayout)
    expect(wrapper.text()).toContain('登录入口')
    expect(wrapper.text()).not.toContain('已登录')
  })

  it('showsUserTagWhenLoggedIn', async () => {
    const { wrapper, pinia } = mountWithPlugins(MainLayout)
    const store = useUserStore(pinia)
    store.setUser({ userId: 1, username: 'alice', phone: '13800000000', email: 'a@example.com' })
    await nextTick()

    expect(wrapper.text()).toContain('已登录：alice')
  })

  it('logoutClearsStoreAndNavigates', async () => {
    const { wrapper, pinia } = mountWithPlugins(MainLayout)
    const store = useUserStore(pinia)
    store.setUser({ userId: 1, username: 'alice', phone: '13800000000', email: 'a@example.com' })
    await nextTick()

    const logoutBtn = wrapper.findAll('button').find((btn) => btn.text().includes('退出登录'))
    await logoutBtn.trigger('click')

    expect(store.userId).toBe(0)
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('rendersRouteMetaTitle', () => {
    const { wrapper } = mountWithPlugins(MainLayout)
    expect(wrapper.find('h1').text()).toBe('首页看板')
  })
})
