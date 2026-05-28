<template>
  <section class="page-grid two-columns">
    <el-card class="content-section" shadow="never">
      <template #header>城市岗位分布</template>
      <div ref="cityChartRef" class="chart-box"></div>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>分析指标</template>
      <el-table :data="metrics" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="指标" />
        <el-table-column prop="value" label="数值" />
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { fetchJobList } from '../api/job.js'

const cityChartRef = ref(null)
const loading = ref(false)
const metrics = ref([
  { name: '平均薪资', value: '-' },
  { name: '岗位数量最高城市', value: '-' },
  { name: '热门技能', value: '-' }
])

let chartInstance = null

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

function handleResize() {
  if (chartInstance) chartInstance.resize()
}

async function loadData() {
  loading.value = true
  try {
    const res = await fetchJobList()
    if (res && res.code === 200) {
      const jobs = res.data || []
      updateChart(jobs)
      updateMetrics(jobs)
    }
  } catch (err) {
    console.error('加载数据失败:', err)
  } finally {
    loading.value = false
  }
}

function updateChart(jobs) {
  // 统计每个城市的岗位数量
  const cityCount = {}
  jobs.forEach(job => {
    if (job.city) {
      cityCount[job.city] = (cityCount[job.city] || 0) + 1
    }
  })

  // 按数量降序排列
  const sortedCities = Object.entries(cityCount)
    .sort((a, b) => b[1] - a[1])

  const cities = sortedCities.map(item => item[0])
  const counts = sortedCities.map(item => item[1])

  if (!chartInstance) {
    chartInstance = echarts.init(cityChartRef.value)
  }

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: cities,
      axisLabel: {
        rotate: 30,
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '岗位数量'
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        xAxisIndex: [0],
        start: 0,
        end: cities.length > 6 ? 60 : 100,
        bottom: 10,
        height: 20
      },
      {
        type: 'inside',
        xAxisIndex: [0],
        start: 0,
        end: cities.length > 6 ? 60 : 100
      }
    ],
    series: [
      {
        type: 'bar',
        data: counts,
        itemStyle: {
          color: '#2563eb',
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '60%'
      }
    ]
  })
}

function updateMetrics(jobs) {
  if (jobs.length === 0) return

  // 平均薪资
  let totalSalary = 0
  let count = 0
  jobs.forEach(job => {
    if (job.salary) {
      const parts = job.salary.split('-')
      if (parts.length === 2) {
        const min = parseInt(parts[0])
        const max = parseInt(parts[1])
        if (!isNaN(min) && !isNaN(max)) {
          totalSalary += (min + max) / 2 / 1000
          count++
        }
      }
    }
  })
  const avgSalary = count > 0 ? (totalSalary / count).toFixed(1) : 0

  // 岗位最多的城市
  const cityCount = {}
  jobs.forEach(job => {
    if (job.city) {
      cityCount[job.city] = (cityCount[job.city] || 0) + 1
    }
  })
  const topCity = Object.entries(cityCount)
    .sort((a, b) => b[1] - a[1])[0]

  // 热门技能
  const skillCount = {}
  jobs.forEach(job => {
    if (job.skillTags) {
      job.skillTags.split(',').forEach(s => {
        const skill = s.trim()
        if (skill) skillCount[skill] = (skillCount[skill] || 0) + 1
      })
    }
  })
  const topSkill = Object.entries(skillCount)
    .sort((a, b) => b[1] - a[1])[0]

  metrics.value = [
    { name: '平均薪资', value: avgSalary + 'K' },
    { name: '岗位数量最高城市', value: topCity ? `${topCity[0]} (${topCity[1]}个)` : '-' },
    { name: '热门技能', value: topSkill ? `${topSkill[0]} (${topSkill[1]}次)` : '-' }
  ]
}
</script>

<style scoped>
.page-grid.two-columns {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}
.chart-box {
  width: 100%;
  height: 400px;
}
</style>
