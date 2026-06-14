import { describe, expect, it } from 'vitest'
import router from './index'

describe('router', () => {
  it('registers the core project pages', () => {
    const routeNames = router.getRoutes().map((route) => route.name)

    expect(routeNames).toContain('dashboard')
    expect(routeNames).toContain('jobs')
    expect(routeNames).toContain('analysis')
    expect(routeNames).toContain('resume')
    expect(routeNames).toContain('resume-history')
    expect(routeNames).toContain('resume-history-detail')
    expect(routeNames).toContain('admin')
    expect(routeNames).toContain('login')
    expect(routeNames).toContain('job-detail')
  })

  it('setsDocumentTitleFromMeta', async () => {
    await router.push({ name: 'dashboard' })
    await router.isReady()

    expect(document.title).toBe('首页看板 - 招聘数据分析平台')
  })
})
