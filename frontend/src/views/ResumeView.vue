<template>
  <div class="page-grid two-columns">
    <el-card class="content-section" shadow="never">
      <template #header>1) 兴趣岗位</template>
      <el-form label-position="top">
        <el-form-item label="用户ID">
          <el-input :model-value="userIdText" disabled />
        </el-form-item>
        <el-form-item label="兴趣岗位（逗号分隔）">
          <el-input v-model="interestText" placeholder="例如：数据分析师,Java后端开发,算法工程师" />
        </el-form-item>
        <el-button type="primary" :loading="savingInterest" @click="handleSaveInterest">保存兴趣岗位</el-button>
      </el-form>
      <el-divider />
      <p v-if="interestList.length === 0">暂无兴趣岗位</p>
      <el-tag v-for="item in interestList" :key="item.jobName" class="interest-tag">
        {{ item.jobName }} (P{{ item.priority }})
      </el-tag>
    </el-card>

    <el-card class="content-section" shadow="never">
      <template #header>2) 上传简历并匹配岗位</template>
      <el-form label-position="top">
        <el-form-item label="简历名称">
          <el-input v-model="resumeForm.resumeName" placeholder="例如：数据分析师简历-2026" />
        </el-form-item>
        <el-form-item label="目标岗位">
          <el-input v-model="resumeForm.targetJobName" placeholder="例如：数据分析师" />
        </el-form-item>
        <el-form-item label="技能（逗号分隔）">
          <el-input v-model="skillsText" placeholder="例如：SQL,Python,Tableau,Java" />
        </el-form-item>
        <el-form-item label="简历文件">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :show-file-list="true"
            accept=".pdf"
          >
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-button type="primary" :loading="matching" @click="handleUploadAndMatch">上传并进入历史详情</el-button>
      </el-form>
    </el-card>
  </div>

  <el-card class="content-section" shadow="never">
    <template #header>3) 匹配结果</template>
    <el-table :data="matchList" style="width: 100%">
      <el-table-column prop="jobName" label="岗位" />
      <el-table-column prop="companyName" label="公司" />
      <el-table-column prop="city" label="城市" width="120" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="skillScore" label="技能分" width="100" />
      <el-table-column prop="experienceScore" label="经验分" width="100" />
      <el-table-column prop="educationScore" label="学历分" width="100" />
      <el-table-column prop="salaryScore" label="薪资分" width="100" />
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { fetchResumeMatches, triggerResumeMatch, uploadResume } from '@/api/resume'
import { fetchInterestJobs, saveInterestJobs } from '@/api/interest'

const userStore = useUserStore()
const router = useRouter()
const savingInterest = ref(false)
const matching = ref(false)
const interestText = ref('')
const skillsText = ref('SQL,Python')
const interestList = ref([])
const matchList = ref([])
const latestResumeId = ref(null)
const selectedFile = ref(null)

const resumeForm = reactive({
  resumeName: '默认简历',
  targetJobName: '数据分析师'
})

const userIdText = computed(() => String(userStore.userId || '未登录'))

function ensureLogin() {
  if (!userStore.userId) {
    ElMessage.warning('请先去登录页注册/登录')
    throw new Error('NO_LOGIN')
  }
}

function parseTextToList(text) {
  return text
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

async function loadInterest() {
  if (!userStore.userId) {
    return
  }
  const result = await fetchInterestJobs(userStore.userId)
  interestList.value = result.data || []
  interestText.value = interestList.value.map((item) => item.jobName).join(',')
}

async function handleSaveInterest() {
  ensureLogin()
  const jobs = parseTextToList(interestText.value).map((jobName, index) => ({
    jobName,
    priority: Math.min(index + 1, 5)
  }))
  if (jobs.length === 0) {
    ElMessage.warning('请至少填写一个兴趣岗位')
    return
  }
  savingInterest.value = true
  try {
    await saveInterestJobs({
      userId: userStore.userId,
      jobs
    })
    ElMessage.success('兴趣岗位保存成功')
    await loadInterest()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '保存失败')
  } finally {
    savingInterest.value = false
  }
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleFileRemove() {
  selectedFile.value = null
}

async function handleUploadAndMatch() {
  const uploadStartedAt = performance.now()
  ensureLogin()
  if (!resumeForm.resumeName.trim()) {
    ElMessage.warning('请填写简历名称')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先选择简历文件')
    return
  }
  matching.value = true
  try {
    const formData = new FormData()
    formData.append('userId', String(userStore.userId))
    formData.append('resumeName', resumeForm.resumeName.trim())
    formData.append('targetJobName', resumeForm.targetJobName.trim())
    formData.append('skillsText', skillsText.value)
    formData.append('file', selectedFile.value)
    const createResult = await uploadResume(formData)
    if (createResult.code !== 200) {
      ElMessage.error(createResult.message || '简历上传失败')
      return
    }
    const uploadElapsedMs = performance.now() - uploadStartedAt
    console.log(`[简历上传] 点击到上传成功耗时: ${uploadElapsedMs.toFixed(1)} ms`)
    const resumeId = createResult.data?.resumeId
    if (resumeId == null) {
      ElMessage.error('上传成功但未返回简历ID')
      return
    }
    latestResumeId.value = resumeId

    const matchTrigger = await triggerResumeMatch(resumeId, 20)
    if (matchTrigger.code !== 200) {
      ElMessage.error(matchTrigger.message || '触发岗位匹配失败')
      return
    }

    const listResult = await fetchResumeMatches(resumeId)
    if (listResult.code !== 200) {
      ElMessage.error(listResult.message || '获取匹配结果失败')
      return
    }
    matchList.value = listResult.data || []
    ElMessage.success(`匹配完成，返回 ${matchList.value.length} 条结果`)
    await router.push(`/resume/history/${resumeId}`)
  } catch (error) {
    const netMsg = error?.response?.data?.message
    ElMessage.error(netMsg || error?.message || '匹配失败（请确认后端已启动且已登录有效账号）')
  } finally {
    matching.value = false
  }
}

onMounted(async () => {
  await loadInterest()
})
</script>
