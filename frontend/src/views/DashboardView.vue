<template>
  <section class="page-grid">
    <el-card class="metric-card" shadow="never">
      <span>岗位总数</span>
      <strong>{{ stats.totalJobs }}</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>覆盖城市</span>
      <strong>{{ stats.cities }}</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>热门技能</span>
      <strong>{{ stats.topSkill }}</strong>
    </el-card>
    <el-card class="metric-card" shadow="never">
      <span>平均薪资</span>
      <strong>{{ stats.avgSalary }}K</strong>
    </el-card>
  </section>

  <el-card class="content-section" shadow="never">
    <template #header>开发入口</template>
    <el-space wrap>
      <el-button type="primary" @click="$router.push('/jobs')">查看岗位</el-button>
      <el-button @click="$router.push('/analysis')">数据分析</el-button>
      <el-button @click="$router.push('/resume')">简历辅助</el-button>
    </el-space>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchJobList } from '../api/job.js'

const stats = ref({
  totalJobs: 0,
  cities: 0,
  topSkill: '-',
  avgSalary: 0
})

async function loadStats() {
  try {
    const res = await fetchJobList()
    if (res && res.code === 200) {
      const jobs = res.data || []
      stats.value.totalJobs = jobs.length
      
      // 统计城市数
      const citySet = new Set(jobs.map(j => j.city))
      stats.value.cities = citySet.size
      
      // 统计热门技能
      const skillCount = {}
      jobs.forEach(job => {
        if (job.skillTags) {
          job.skillTags.split(',').forEach(s => {
            const skill = s.trim()
            if (skill) skillCount[skill] = (skillCount[skill] || 0) + 1
          })
        }
      })
      const sortedSkills = Object.entries(skillCount).sort((a, b) => b[1] - a[1])
      stats.value.topSkill = sortedSkills.length > 0 ? sortedSkills[0][0] : '-'
      
      // 计算平均薪资（取中间值）
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
      stats.value.avgSalary = count > 0 ? (totalSalary / count).toFixed(1) : 0
    }
  } catch (err) {
    console.error('加载统计数据失败:', err)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.page-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.metric-card {
  text-align: center;
}
.metric-card span {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}
.metric-card strong {
  display: block;
  font-size: 28px;
  color: #333;
}
</style>
