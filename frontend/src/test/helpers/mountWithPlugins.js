import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import { vi } from 'vitest'

export function mountWithPlugins(component, options = {}) {
  const pinia = options.pinia ?? createPinia()
  setActivePinia(pinia)
  options.configurePinia?.(pinia)
  const route = options.route ?? { path: '/', meta: {}, params: {} }
  const routerPush = options.routerPush ?? vi.fn()

  const wrapper = mount(component, {
    ...options,
    global: {
      plugins: [pinia, ElementPlus],
      stubs: {
        'router-view': true,
        ...(options.global?.stubs || {})
      },
      mocks: {
        $route: route,
        $router: { push: routerPush },
        ...(options.global?.mocks || {})
      },
      ...options.global
    }
  })

  return { wrapper, routerPush, pinia }
}

export { flushPromises }
