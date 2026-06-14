<template>
  <div class="detail-stack" v-loading="loading">
    <el-card class="content-section" shadow="never">
      <template #header>
        <div class="history-header">
          <span>简历详情</span>
          <el-space>
            <el-button plain @click="$router.push('/resume/history')">返回历史</el-button>
            <el-button type="primary" plain @click="$router.push('/resume')">继续上传</el-button>
          </el-space>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="简历名称">{{ detail.resumeName }}</el-descriptions-item>
        <el-descriptions-item label="目标岗位">{{ detail.targetJobName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">{{ detail.fileType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="解析状态">{{ detail.parseStatus === 0 ? '待解析' : '已保存' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>
        <div class="history-header">
          <span>岗位选择建议（DeepSeek）</span>
          <el-button type="primary" :loading="adviceLoading" @click="handleGenerateAdvice">
            生成岗位选择建议
          </el-button>
        </div>
      </template>
      <p v-if="detail.jobSelectionAdviceModel" class="muted">模型：{{ detail.jobSelectionAdviceModel }}</p>
      <pre v-if="detail.jobSelectionAdvice" class="text-panel advice-panel">{{ detail.jobSelectionAdvice }}</pre>
      <p v-else class="muted">暂无建议，点击上方按钮生成（需配置后端环境变量 DEEPSEEK_API_KEY）。</p>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>
        <div class="history-header">
          <span>兴趣岗位简历修改建议（DeepSeek）</span>
          <el-button type="primary" :loading="interestAdviceLoading" @click="handleInterestResumeAdvice">
            按兴趣岗位生成简历修改建议
          </el-button>
        </div>
      </template>
      <p class="muted">使用你在「简历辅助」中保存的兴趣岗位名称；请先保存兴趣岗位再生成。</p>
      <p v-if="detail.interestResumeAdviceModel" class="muted">模型：{{ detail.interestResumeAdviceModel }}</p>
      <pre v-if="detail.interestResumeAdvice" class="text-panel advice-panel">{{ detail.interestResumeAdvice }}</pre>
      <p v-else class="muted">暂无建议，点击上方按钮生成（需 DEEPSEEK_API_KEY）。</p>
    </el-card>

    <div class="page-grid two-columns">
      <el-card class="content-section" shadow="never">
        <template #header>解析文本</template>
        <pre class="text-panel">{{ detail.resumeText || '暂无解析文本' }}</pre>
      </el-card>

      <el-card class="content-section" shadow="never">
        <template #header>解析摘要</template>
        <div v-if="detail.parseResult">
          <p><strong>建议：</strong>{{ detail.parseResult.suggestions || '-' }}</p>
          <p><strong>主要专业：</strong>{{ detail.parseResult.parsedMajor || '-' }}</p>
          <p><strong>学校：</strong>{{ detail.parseResult.parsedSchool || '-' }}</p>
          <p><strong>学历：</strong>{{ detail.parseResult.parsedEducation || '-' }}</p>
        </div>
        <p v-else>暂无解析摘要</p>
      </el-card>
    </div>

    <div class="page-grid two-columns">
      <el-card class="content-section" shadow="never">
        <template #header>已保存技能</template>
        <el-space wrap>
          <el-tag v-for="skill in detail.skills" :key="skill.id" type="success">
            {{ skill.skillName }}
          </el-tag>
        </el-space>
        <p v-if="detail.skills.length === 0">暂无技能记录</p>
      </el-card>

      <el-card class="content-section" shadow="never">
        <template #header>匹配结果</template>
        <el-table :data="detail.matches" style="width: 100%">
          <el-table-column prop="jobName" label="岗位" />
          <el-table-column prop="companyName" label="公司" />
          <el-table-column prop="totalScore" label="总分" width="90" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchResumeHistoryDetail, generateJobSelectionAdvice, generateInterestResumeAdvice } from '@/api/resume'

const route = useRoute()
const loading = ref(false)
const adviceLoading = ref(false)
const interestAdviceLoading = ref(false)
const detail = reactive({
  resumeId: null,
  resumeName: '',
  fileUrl: '',
  fileType: '',
  targetJobName: '',
  parseStatus: 0,
  resumeText: '',
  skills: [],
  parseResult: null,
  matches: [],
  jobSelectionAdvice: '',
  jobSelectionAdviceModel: '',
  interestResumeAdvice: '',
  interestResumeAdviceModel: ''
})

async function loadDetail() {
  loading.value = true
  try {
    const result = await fetchResumeHistoryDetail(route.params.id)
    if (result.code !== 200) {
      ElMessage.error(result.message || '获取详情失败')
      return
    }
    Object.assign(detail, result.data || {})
    detail.skills = result.data?.skills || []
    detail.matches = result.data?.matches || []
    detail.jobSelectionAdvice = result.data?.jobSelectionAdvice || ''
    detail.jobSelectionAdviceModel = result.data?.jobSelectionAdviceModel || ''
    detail.interestResumeAdvice = result.data?.interestResumeAdvice || ''
    detail.interestResumeAdviceModel = result.data?.interestResumeAdviceModel || ''
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

async function handleGenerateAdvice() {
  const startedAt = performance.now()
  adviceLoading.value = true
  try {
    const result = await generateJobSelectionAdvice(route.params.id)
    if (result.code !== 200) {
      ElMessage.error(result.message || '生成失败')
      return
    }
    const data = result.data || {}
    detail.jobSelectionAdvice = data.advice || ''
    detail.jobSelectionAdviceModel = data.model || ''
    console.log(
      `[DeepSeek] 岗位选择建议 点击到生成成功耗时: ${(performance.now() - startedAt).toFixed(1)} ms`
    )
    ElMessage.success('岗位选择建议已生成')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '生成失败')
  } finally {
    adviceLoading.value = false
  }
}

async function handleInterestResumeAdvice() {
  const startedAt = performance.now()
  interestAdviceLoading.value = true
  try {
    const result = await generateInterestResumeAdvice(route.params.id)
    if (result.code !== 200) {
      ElMessage.error(result.message || '生成失败')
      return
    }
    const data = result.data || {}
    detail.interestResumeAdvice = data.advice || ''
    detail.interestResumeAdviceModel = data.model || ''
    console.log(
      `[DeepSeek] 兴趣岗位简历建议 点击到生成成功耗时: ${(performance.now() - startedAt).toFixed(1)} ms`
    )
    ElMessage.success('兴趣岗位简历建议已生成')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '生成失败')
  } finally {
    interestAdviceLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.muted {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.advice-panel {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
