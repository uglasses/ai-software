import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from './user'

describe('user store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
  })

  it('hydrates profile from localStorage on init', () => {
    localStorage.setItem('userId', '5')
    localStorage.setItem('username', 'alice')
    localStorage.setItem('phone', '13800000000')
    localStorage.setItem('email', 'a@example.com')

    setActivePinia(createPinia())
    const store = useUserStore()

    expect(store.userId).toBe(5)
    expect(store.profile.username).toBe('alice')
    expect(store.profile.phone).toBe('13800000000')
    expect(store.profile.email).toBe('a@example.com')
    expect(store.profile.role).toBe('student')
  })

  it('setUser updates state and localStorage', () => {
    const store = useUserStore()

    store.setUser({
      userId: 10,
      username: 'bob',
      phone: '13900000001',
      email: 'b@example.com'
    })

    expect(store.userId).toBe(10)
    expect(store.profile.username).toBe('bob')
    expect(localStorage.getItem('userId')).toBe('10')
    expect(localStorage.getItem('username')).toBe('bob')
  })

  it('setToken stores token', () => {
    const store = useUserStore()
    store.setToken('jwt-token')
    expect(store.token).toBe('jwt-token')
  })

  it('logout clears state and localStorage', () => {
    const store = useUserStore()
    store.setUser({ userId: 1, username: 'u', phone: '1', email: 'e@e.com' })
    store.setToken('t')

    store.logout()

    expect(store.userId).toBe(0)
    expect(store.token).toBe('')
    expect(store.profile.username).toBe('')
    expect(localStorage.getItem('userId')).toBeNull()
    expect(localStorage.getItem('username')).toBeNull()
  })
})
