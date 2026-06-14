import { describe, expect, it } from 'vitest'
import { mountWithPlugins } from '@/test/helpers/mountWithPlugins'
import JobDetailView from './JobDetailView.vue'

describe('JobDetailView', () => {
  it('rendersRouteJobId', () => {
    const { wrapper } = mountWithPlugins(JobDetailView, {
      route: { path: '/jobs/42', params: { id: '42' }, meta: {} }
    })

    expect(wrapper.text()).toContain('42')
    expect(wrapper.text()).toContain('岗位详情')
  })
})
