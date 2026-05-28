<template>
  <el-card class="content-section" shadow="never">
    <template #header>岗位列表</template>

    <el-form class="filter-bar" inline>
      <el-form-item label="城市">
        <el-select v-model="filters.city" placeholder="选择城市" clearable style="width: 120px">
          <el-option label="北京" value="北京" />
          <el-option label="上海" value="上海" />
          <el-option label="广州" value="广州" />
          <el-option label="深圳" value="深圳" />
          <el-option label="杭州" value="杭州" />
          <el-option label="成都" value="成都" />
          <el-option label="南京" value="南京" />
          <el-option label="武汉" value="武汉" />
          <el-option label="西安" value="西安" />
          <el-option label="苏州" value="苏州" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" placeholder="岗位或技能" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="学历">
        <el-select v-model="filters.education" placeholder="学历要求" clearable style="width: 120px">
          <el-option label="大专" value="大专" />
          <el-option label="本科" value="本科" />
          <el-option label="硕士" value="硕士" />
          <el-option label="不限" value="不限" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">筛选</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="paginatedJobs" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="jobName" label="岗位" />
      <el-table-column prop="companyName" label="公司" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column prop="salary" label="薪资" width="120" />
      <el-table-column prop="education" label="学历" width="100" />
      <el-table-column prop="skillTags" label="技能标签" show-overflow-tooltip />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/jobs/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="filteredJobs.length"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { fetchJobList } from '../api/job.js'

const loading = ref(false)
const allJobs = ref([])

const filters = reactive({
  city: '',
  keyword: '',
  education: ''
})

const pagination = reactive({
  page: 1,
  size: 20
})

const filteredJobs = computed(() => {
  return allJobs.value.filter(job => {
    if (filters.city && job.city !== filters.city) return false
    if (filters.education && job.education !== filters.education) return false
    if (filters.keyword) {
      const kw = filters.keyword.toLowerCase()
      const match = (
        (job.jobName && job.jobName.toLowerCase().includes(kw)) ||
        (job.companyName && job.companyName.toLowerCase().includes(kw)) ||
        (job.skillTags && job.skillTags.toLowerCase().includes(kw))
      )
      if (!match) return false
    }
    return true
  })
})

const paginatedJobs = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return filteredJobs.value.slice(start, end)
})

async function loadJobs() {
  loading.value = true
  try {
    const res = await fetchJobList()
    if (res && res.code === 200) {
      allJobs.value = res.data || []
    } else {
      console.error('获取岗位列表失败:', res)
    }
  } catch (err) {
    console.error('请求岗位列表出错:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
}

function handleReset() {
  filters.city = ''
  filters.keyword = ''
  filters.education = ''
  pagination.page = 1
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
}

function handlePageChange(page) {
  pagination.page = page
}

onMounted(() => {
  loadJobs()
})
</script>

<style scoped>
.filter-bar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
