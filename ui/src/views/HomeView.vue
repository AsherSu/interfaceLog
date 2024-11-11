<script lang="ts" setup>
import { VCard, VPageHeader, VPagination, VTabbar } from "@halo-dev/components"
import { axiosInstance } from "@halo-dev/api-client"
import { onMounted, ref, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { JsonViewer } from 'vue3-json-viewer'
import 'vue3-json-viewer/dist/index.css'
// 导入 echarts
import * as echarts from 'echarts'

// Types
interface ListItem {
  value: string
  label: string
}

interface RuleInfo {
  spec: {
    id: number
    isInclude: boolean
    rule: string
    version: string
  }
  apiVersion: string
  kind: string
  metadata: {
    name: string
    version: number
    creationTimestamp: string
  }
}

interface RuleTableItem {
  id: string
  rule: string
  type: 'include' | 'exclude'
  creationTimestamp: string
  name: string
}

// Navigation
const navigationItems = [
  { id: "1", label: "接口日志", icon: "icon-log" },
  { id: "2", label: "基本设置", icon: "icon-log" },
]
const activeNavId = ref("1")

// Watch navigation changes
watch(activeNavId, (newId) => {
  if (newId === "2") {
    getRules()
    fetchStats()
    fetchRetentionDays()
    fetchCleanStats()
  }
})

// 也可以在模板中直
const handleNavChange = (id: string) => {
  activeNavId.value = id
  if (id === "2") {
    getRules()
    fetchStats()
  }
}

// Table State
const logTableData = ref<any>([])
const isTableLoading = ref(true)

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

// Search Filters
const searchDateRange = ref('')
const selectedUsers = ref<string[]>([])
const selectedClientIps = ref<string[]>([])
const selectedPaths = ref<string[]>([])

// Filter Options
const userOptions = ref<ListItem[]>([])
const clientIpOptions = ref<ListItem[]>([])
const pathOptions = ref<ListItem[]>([])
const isUserOptionsLoading = ref(false)
const isClientIpOptionsLoading = ref(false)
const isPathOptionsLoading = ref(false)

// Date Shortcuts
const dateShortcuts = [
  {
    text: 'Last week',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: 'Last month',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: 'Last 3 months',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    },
  },
]

// Detail Dialog
const detailDialogVisible = ref(false)
const currentDetail = ref<any>(null)

// Search Methods
const searchUsers = async (query: string) => {
  isUserOptionsLoading.value = true
  try {
    const response = await axiosInstance.get(`/apis/dailyActive.halo.run/v1alpha1/interfaceLog/users?username=${query || ''}&size=10`)
    userOptions.value = response.data
  } catch (error) {
    console.error("Error fetching users:", error)
  } finally {
    isUserOptionsLoading.value = false
  }
}

const searchClientIps = async (query: string) => {
  isClientIpOptionsLoading.value = true
  try {
    const response = await axiosInstance.get(`/apis/dailyActive.halo.run/v1alpha1/interfaceLog/clientIps?clientIp=${query || ''}&size=10`)
    clientIpOptions.value = response.data
  } catch (error) {
    console.error("Error fetching client IPs:", error)
  } finally {
    isClientIpOptionsLoading.value = false
  }
}

const searchPaths = async (query: string) => {
  isPathOptionsLoading.value = true
  try {
    const response = await axiosInstance.get(`/apis/dailyActive.halo.run/v1alpha1/interfaceLog/paths?path=${query || ''}&size=10`)
    pathOptions.value = response.data
  } catch (error) {
    console.error("Error fetching paths:", error)
  } finally {
    isPathOptionsLoading.value = false
  }
}

// Data Loading
async function fetchLogData(pageNum: number) {
  const searchParams = {
    page: pageNum,
    size: pageSize.value,
    username: selectedUsers.value.map(user => user.substring(1)),
    clientIp: selectedClientIps.value,
    path: selectedPaths.value,
    accessTimes: searchDateRange.value || [],
  }

  isTableLoading.value = true
  try {
    const response = await axiosInstance.post(
      `/apis/dailyActive.halo.run/v1alpha1/interfaceLog/search`, 
      removeCircularReferences(searchParams)
    )
    const { page, size, total, items } = response.data
    
    currentPage.value = page
    pageSize.value = size
    totalItems.value = total
    logTableData.value = items.map(mapLogItem)
  } catch (error) {
    console.error('Error fetching log data:', error)
    ElMessage.error('获取日志数据失败')
  } finally {
    isTableLoading.value = false
  }
}

// Event Handlers
async function handlePageChange(newPage: number) {
  await fetchLogData(newPage)
}

async function handleDetailView(row: any) {
  try {
    const response = await axiosInstance.get(`/apis/dailyActive.halo.run/v1alpha1/interfaceLogInfos/${row.id}`)
    currentDetail.value = response.data.spec
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// Utility Functions
function mapLogItem(item: any) {
  return {
    id: item.id,
    accessTime: item.accessTime,
    username: item.username,
    clientIp: item.clientIp,
    path: item.path,
    requestType: item.requestType,
    responseStatus: item.responseStatus,
  }
}

function removeCircularReferences(obj: any) {
  const seen = new WeakSet()
  return JSON.parse(JSON.stringify(obj, (key, value) => {
    if (typeof value === 'object' && value !== null) {
      if (seen.has(value)) return
      seen.add(value)
    }
    return value
  }))
}

function parseJson(value: any): any {
  if (!value) return null
  try {
    return typeof value === 'string' ? JSON.parse(value) : value
  } catch (error) {
    console.error('JSON parse error:', error)
    return value
  }
}

function getTableRowClassName({ row }: { row: any }) {
  return !row.responseStatus.startsWith('2') ? 'warning-row' : ''
}

// Initialization
onMounted(() => {
  fetchLogData(1)
})

// Settings Tab State
const ruleTableData = ref<RuleTableItem[]>([])
const isLoading = ref(false)
const dialogFormVisible = ref(false)
const ruleForm = ref({
  id: 0,
  rule: '',
  type: 'include',
  name: ''
})
const formLabelWidth = '80px'

// Settings Tab Methods
const addInput = () => {
  ruleForm.value = {
    id: 0,
    rule: '',
    type: 'include',
    name: ''
  }
  dialogFormVisible.value = true
}

// 获取规则列表
const getRules = async () => {
  isLoading.value = true
  try {
    const response = await axiosInstance.get('/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/get')
    const rules = response.data as RuleInfo[]
    
    ruleTableData.value = rules.map(item => ({
      id: item.metadata.name,
      rule: item.spec.rule,
      type: item.spec.isInclude ? 'include' as const : 'exclude' as const,
      creationTimestamp: item.metadata.creationTimestamp,
      name: item.metadata.name
    }))
  } catch (error) {
    console.error('获取规则列表失败:', error)
    ElMessage.error('取规则列表失败')
  } finally {
    isLoading.value = false
  }
}

// 添加状态量
const deleteDialogVisible = ref(false)
const deleteRow = ref<RuleTableItem | null>(null)

// 修改删除处理方法
const handleDelete = (row: RuleTableItem) => {
  deleteRow.value = row
  deleteDialogVisible.value = true
}

// 确除方法
const confirmDelete = async () => {
  if (!deleteRow.value) return
  
  isLoading.value = true
  try {
    const requestBody = {
      apiVersion: "dailyActive.halo.run/v1alpha1",
      kind: "InterfaceLogRuleInfo",
      metadata: {
        name: deleteRow.value.id
      },
      spec: {
        id: deleteRow.value.id,
        isInclude: deleteRow.value.type === 'include',
        rule: deleteRow.value.rule,
        version: "1"
      }
    }

    const { status } = await axiosInstance.delete(
      '/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/delete',
      { data: requestBody }
    )

    if (status === 200) {
      deleteDialogVisible.value = false
      ElMessage.success('删除成功')
      await new Promise(resolve => setTimeout(resolve, 100))
      await getRules()
    }
  } catch (error) {
    console.error('删除规则失败:', error)
    ElMessage.error('删除失败')
  } finally {
    isLoading.value = false
  }
}

// 保存规则
const saveRule = async () => {
  if (!ruleForm.value.rule) {
    ElMessage.warning('请输入规则内容')
    return
  }

  isLoading.value = true
  try {
    const isEdit = Boolean(ruleForm.value.id)
    const requestBody = {
      apiVersion: "dailyActive.halo.run/v1alpha1",
      kind: "InterfaceLogRuleInfo",
      metadata: {
        name: isEdit ? ruleForm.value.name : Date.now().toString()
      },
      spec: {
        id: ruleForm.value.id || 0,
        isInclude: ruleForm.value.type === 'include',
        rule: ruleForm.value.rule,
        version: "1"
      }
    }

    const { status } = await axiosInstance[isEdit ? 'put' : 'post'](
      isEdit 
        ? `/apis/dailyActive.halo.run/v1alpha1/interfaceLogRuleInfos/${ruleForm.value.name}`
        : '/apis/dailyActive.halo.run/v1alpha1/interfaceLogRuleInfos',
      requestBody
    )

    if (status === (isEdit ? 200 : 201)) {
      dialogFormVisible.value = false
      ElMessage.success(isEdit ? '更新成功' : '添加成功')
      await new Promise(resolve => setTimeout(resolve, 100))
      await getRules()
    }
  } catch (error) {
    console.error(ruleForm.value.id ? '更新规则失败:' : '新增规则失败:', error)
    ElMessage.error(ruleForm.value.id ? '更新失败' : '新增失败')
  } finally {
    isLoading.value = false
  }
}

// 日志表格的行类名方法
const getLogTableRowClassName = ({ row }: { row: any }) => {
  return row.responseStatus && !row.responseStatus.startsWith('2') ? 'warning-row' : ''
}

// 规则表格的类名法
const getRuleTableRowClassName = ({ row }: { row: RuleTableItem }) => {
  return row.type === 'include' ? 'include-row' : 'exclude-row'
}

// 新增相关
const addDialogVisible = ref(false)
const newRuleForm = ref({
  rule: '',
  type: 'include' as 'include' | 'exclude'
})

const handleAdd = () => {
  newRuleForm.value = {
    rule: '',
    type: 'include'
  }
  addDialogVisible.value = true
}

const saveNewRule = async () => {
  if (!newRuleForm.value.rule) {
    ElMessage.warning('请输入规则内容')
    return
  }

  isLoading.value = true
  try {
    const requestBody = {
      apiVersion: "dailyActive.halo.run/v1alpha1",
      kind: "InterfaceLogRuleInfo",
      metadata: {
        name: Date.now().toString()
      },
      spec: {
        id: Date.now().toString(),
        isInclude: newRuleForm.value.type === 'include',
        rule: newRuleForm.value.rule,
        version: "1"
      }
    }

    const { status } = await axiosInstance.post(
      '/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/create',
      requestBody
    )

    if (status === 200) {
      addDialogVisible.value = false
      ElMessage.success('添加成功')
      await new Promise(resolve => setTimeout(resolve, 100))
      await getRules()
    }
  } catch (error) {
    console.error('新规则失败:', error)
    ElMessage.error('新增失败')
  } finally {
    isLoading.value = false
  }
}

// 编辑相关
const editDialogVisible = ref(false)
const editRuleForm = ref({
  id: '',
  rule: '',
  type: 'include' as 'include' | 'exclude',
  name: ''
})

const handleEdit = (row: RuleTableItem) => {
  editRuleForm.value = {
    id: row.id,
    rule: row.rule,
    type: row.type,
    name: row.name
  }
  editDialogVisible.value = true
}

const updateRule = async () => {
  if (!editRuleForm.value.rule) {
    ElMessage.warning('请输入规则内容')
    return
  }

  isLoading.value = true
  try {
    const requestBody = {
      apiVersion: "dailyActive.halo.run/v1alpha1",
      kind: "InterfaceLogRuleInfo",
      metadata: {
        name: editRuleForm.value.id
      },
      spec: {
        id: editRuleForm.value.id,
        isInclude: editRuleForm.value.type === 'include',
        rule: editRuleForm.value.rule,
        version: "1"
      }
    }

    const { status } = await axiosInstance.put(
      '/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/update',
      requestBody
    )

    if (status === 200) {
      editDialogVisible.value = false
      ElMessage.success('更新成功')
      await new Promise(resolve => setTimeout(resolve, 100))
      await getRules()
    }
  } catch (error) {
    console.error('更新规则失败:', error)
    ElMessage.error('更新失败')
  } finally {
    isLoading.value = false
  }
}

// 在模板中根据是否有 id 调用不同的法
const handleSave = () => {
  if (ruleForm.value.id) {
    updateRule()
  } else {
    saveNewRule()
  }
}

// 日志保留时间相关状态
const retentionDays = ref(7)  // 默认7天
const isSavingRetention = ref(false)

// 清理相关状态
const cleanDialogVisible = ref(false)
const isCleaningLogs = ref(false)

// 添加日志统计态

// 修改显示清理对话框方法
const handleCleanLogs = async () => {
  // 直接显示清理对话框，而不调用任何接口
  cleanDialogVisible.value = true
  await fetchCleanStats()
}

// 确认清理
const confirmClean = async () => {
  isCleaningLogs.value = true
  try {
    const response = await axiosInstance.delete(
      '/apis/dailyActive.halo.run/v1alpha1/interfaceLog/deleteAll'
    )
    
    if (response.status === 200) {
      cleanDialogVisible.value = false
      ElMessage.success('清理成功')
      // 如果需要刷新日志列表
      if (activeNavId.value === '1') {
        await fetchLogData(1)
      }
    }
  } catch (error) {
    console.error('清理日志失败:', error)
    ElMessage.error('清理失败')
  } finally {
    isCleaningLogs.value = false
  }
}

// 保存保留时间
const saveRetentionDays = async () => {
  if (!retentionDays.value) {
    ElMessage.warning('请输入保留天数')
    return
  }

  isSavingRetention.value = true
  try {
    const response = await axiosInstance.post(
      '/apis/dailyActive.halo.run/v1alpha1/retentionDuration/set',
      { days: retentionDays.value }
    )
    
    if (response.status === 200) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存保留时间失败:', error)
    ElMessage.error('保存失败')
  } finally {
    isSavingRetention.value = false
  }
}

// 获取保留时

// 添加统计相关的状态
const storageStats = ref({
  usedStorage: 0,
  totalStorage: 0,
  usagePercentage: 0
})

const memoryStats = ref({
  usedMemory: 0,
  totalMemory: 0,
  usagePercentage: 0
})

const logStats = ref({
  totalCount: 0,
  todayCount: 0
})

// 添加七日统计数据的状态
const weeklyStats = ref({
  dates: [],
  counts: []
})

// 获取统计信息时添加七日统计数据的获取
const fetchStats = async () => {
  try {
    const response = await axiosInstance.get('/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/systemInfo')
    const { totalSpace, unUsableSpace, totalMemory, unUsableMemory } = response.data
    
    // 更新存储统计
    storageStats.value = {
      usedStorage: unUsableSpace,
      totalStorage: totalSpace,
      usagePercentage: Math.round((unUsableSpace / totalSpace) * 100)
    }
    
    // 更新内存统计
    memoryStats.value = {
      usedMemory: unUsableMemory,
      totalMemory: totalMemory,
      usagePercentage: Math.round((unUsableMemory / totalMemory) * 100)
    }

    // 更新图表
    updateChart()
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 更新图表的方法
const updateChart = () => {
  const chartDom = document.getElementById('statsChart')
  if (chartDom) {
    const myChart = echarts.init(chartDom)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: weeklyStats.value.dates,
        axisLabel: {
          rotate: 45
        }
      },
      yAxis: {
        type: 'value',
        name: '日志数量'
      },
      series: [{
        data: weeklyStats.value.counts,
        type: 'line',
        smooth: true,
        name: '日志数量',
        areaStyle: {
          opacity: 0.3
        }
      }]
    }
    myChart.setOption(option)
  }
}

// 格式化大小
const formatSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化数字（添加千分位）
const formatNumber = (num: number) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",")
}

// 进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 70) return '#67C23A'
  if (percentage < 90) return '#E6A23C'
  return '#F56C6C'
}

// 在组件挂载时获取统计信息
onMounted(() => {
  fetchStats()
})

// 定时刷新统计信息（每分钟）
let statsTimer: ReturnType<typeof setInterval>
watch(activeNavId, (newId) => {
  // 清除已存在的定时器
  if (statsTimer) {
    clearInterval(statsTimer)
  }
  
  // 只在设置页面启动定时器
  if (newId === "2") {
    // 每5秒调用一次
    statsTimer = setInterval(() => {
      fetchStats()
    }, 5000)
  }
})

onUnmounted(() => {
  if (statsTimer) {
    clearInterval(statsTimer)
  }
})

// 初始化图表
onMounted(() => {
  const chartDom = document.getElementById('statsChart')
  if (chartDom) {
    const myChart = echarts.init(chartDom)
    const option = {
      xAxis: {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: [150, 230, 224, 218, 135, 147, 260],
        type: 'line',
        smooth: true
      }]
    }
    myChart.setOption(option)
  }
})

const handleImport = () => {
  // 创建文件输入元素
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  
  input.onchange = async (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      try {
        const reader = new FileReader()
        reader.onload = async (e) => {
          try {
            const result = e.target?.result
            if (typeof result !== 'string') return
            const rules = JSON.parse(result)
            isLoading.value = true
            
            // 调用后端导入接口
            const { status } = await axiosInstance.post(
              '/apis/dailyActive.halo.run/v1alpha1/interfaceLogRule/import',
              rules
            )
            
            if (status === 200) {
              ElMessage.success('导入成功')
              await getRules()
            }
          } catch (error) {
            console.error('导入规则失败:', error)
            ElMessage.error('导入失败')
          } finally {
            isLoading.value = false
          }
        }
        reader.readAsText(file)
      } catch (error) {
        ElMessage.error('文件读取失败')
      }
    }
  }
  
  input.click()
}

const handleExport = () => {
  // 创建 InterfaceLogRuleFileVO 格式的数据，包含 isInclude, rule, 和 version 字段
  const interfaceLogRuleFileVO = {
    interfaceLogRules: ruleTableData.value.map(rule => ({
      isInclude: rule.type === 'include', // 假设 type 字段为 'include' 或 'exclude'
      rule: rule.rule,
      version: "1" // 假设版本号为固定值 "1"
    }))
  };

  // 将数据转换为 JSON 字符串
  const jsonString = JSON.stringify(interfaceLogRuleFileVO, null, 2);

  // 创建一个 Blob 对象并触发下载
  const blob = new Blob([jsonString], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `interface-log-rules-${new Date().getTime()}.json`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

// 获取保留时间
const fetchRetentionDays = async () => {
  try {
    const response = await axiosInstance.get('/apis/dailyActive.halo.run/v1alpha1/retentionDuration/get')
    const data = response.data

    // 提取 day 字段的值
    if (data && data.spec && typeof data.spec.day === 'number') {
      retentionDays.value = data.spec.day
    } else {
      console.error('Invalid response structure:', data)
      ElMessage.error('获取保留时间失败')
    }
  } catch (error) {
    console.error('获取保留时间失败:', error)
    ElMessage.error('获取保留时间失败')
  }
}

// 手动调用 fetchRetentionDays 时使用
// fetchRetentionDays()

// 清理数据状态
const cleanStats = ref({ totalCount: 0 })

// 获取待清理数据
const fetchCleanStats = async () => {
  try {
    const response = await axiosInstance.get('/apis/dailyActive.halo.run/v1alpha1/interfaceLog/count')
    cleanStats.value.totalCount = response.data
  } catch (error) {
    console.error('获取待清理数据失败:', error)
    ElMessage.error('获取待清理数据失败')
  }
}
</script>
<template>
  <VPageHeader title="接口日志"></VPageHeader>
  <VCard class="m-0 flex-1 md:m-4">
    <div class="p-3">
      <VTabbar
        v-model:activeId="activeNavId"
        :items="navigationItems"
        :type="'pills'"
        :direction="'row'"
      />
    </div>
    <div v-if="activeNavId==='1'" class="mx-auto max-w-4xl px-4 md:px-8">
      <el-config-provider :value-on-clear="null" :empty-values="[undefined, null]">
        <div class="flex flex-wrap gap-4 items-center">
          <el-select
            v-model="selectedUsers"
            multiple
            filterable
            clearable
            remote
            collapse-tags
            reserve-keyword
            remote-show-suffix
            collapse-tags-tooltip
            :default-first-option="true"
            placeholder="用户名"
            :remote-method="searchUsers"
            :loading="isUserOptionsLoading"
            style="width: 230px"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.value"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="selectedClientIps"
            multiple
            filterable
            remote
            collapse-tags
            reserve-keyword
            remote-show-suffix
            collapse-tags-tooltip
            :default-first-option="true"
            placeholder="请求IP"
            :remote-method="searchClientIps"
            :loading="isClientIpOptionsLoading"
            style="width: 230px"
          >
            <el-option
              v-for="item in clientIpOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-model="selectedPaths"
            multiple
            filterable
            remote
            collapse-tags
            reserve-keyword
            remote-show-suffix
            collapse-tags-tooltip
            :default-first-option="true"
            placeholder="接口路径"
            :remote-method="searchPaths"
            :loading="isPathOptionsLoading"
            style="width: 230px"
          >
            <el-option
              v-for="item in pathOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div class="block">
            <el-date-picker
              v-model="searchDateRange"
              type="datetimerange"
              :shortcuts="dateShortcuts"
              range-separator="to"
              start-placeholder="Start date"
              end-placeholder="End date"
            />
          </div>
          <el-button style="color: #2f81f7;--el-button-hover-bg-color: v-bind('blue')" :dark="true"
                     @click="fetchLogData(1)">查询
          </el-button>
        </div>
      </el-config-provider>
      <VCard class="m-0 flex-1 md:m-4">
        <div class="mt-4">
          <el-table
            v-loading="isTableLoading"
            :data="logTableData"
            style="width: 100%; height: calc(100vh - 600px)"
            :max-height="'calc(100vh - 600px)'"
            :row-class-name="getLogTableRowClassName">
            <el-table-column
              prop="accessTime"
              label="请求时间"
              width="240">
            </el-table-column>
            <el-table-column
              prop="username"
              label="户名"
              width="180">
            </el-table-column>
            <el-table-column
              prop="clientIp"
              label="请求IP"
              width="180">
            </el-table-column>
            <el-table-column
              prop="path"
              label="请求URI"
              width="180"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column
              prop="requestType"
              label="请求类型"
              width="180">
            </el-table-column>
            <el-table-column
              prop="responseStatus"
              label="响应码"
              width="180"
              show-overflow-tooltip>
            </el-table-column>
            <el-table-column align="right">
              <template #default="scope">
                <el-button size="small" @click="handleDetailView(scope.row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </VCard>
      <VPagination v-model:page="currentPage" v-model:size="pageSize" v-model:total="totalItems" @update:page="handlePageChange"/>
    </div>
    <div v-else-if="activeNavId==='2'">
      <VCard class="m-0 flex-1 md:m-4" title="记录接口">
        <div class="mb-4 flex justify-between items-center">
          <div>
            <el-button 
              type="primary" 
              @click="handleAdd"
              style="--el-button-text-color: #000000; --el-button-bg-color: #409eff; width: 100px;"
            >增加规则</el-button>
          </div>
          <div class="flex gap-2">
            <el-button 
              type="primary" 
              @click="handleImport"
              style="--el-button-text-color: #000000; --el-button-bg-color: #409eff; width: 100px;"
            >导入规则</el-button>
            <el-button 
              type="primary" 
              @click="handleExport"
              style="--el-button-text-color: #000000; --el-button-bg-color: #409eff; width: 100px;"
            >导出规则</el-button>
          </div>
        </div>
        <el-table
          v-loading="isLoading"
          :data="ruleTableData"
          :row-class-name="getRuleTableRowClassName"
          style="width: 100%; height: calc(100vh - 600px)"
          :max-height="'calc(100vh - 600px)'"
        >
          <el-table-column
            prop="id"
            label="ID"
            width="180"/>
          <el-table-column
            prop="rule"
            label="规则"
            width="auto"/>
          <el-table-column
            prop="type"
            label="类型"
            width="100">
            <template #default="scope">
              {{ scope.row.type === 'include' ? '包含' : '排除' }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="140">
            <template #default="scope">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-button
                  type="primary"
                  text
                  size="small"
                  @click="handleEdit(scope.row)"
                  style="width: 50px; min-width: 50px;"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  text
                  size="small"
                  style="color: black; width: 50px; min-width: 50px;"
                  @click="handleDelete(scope.row)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-dialog 
          v-model="dialogFormVisible" 
          :title="ruleForm.id ? '编辑规则' : '新增规则'" 
          width="500">
          <el-form :model="ruleForm">
            <el-form-item label="规则内容" :label-width="formLabelWidth">
              <el-input v-model="ruleForm.rule" autocomplete="off" />
            </el-form-item>
            <el-form-item label="规则类型" :label-width="formLabelWidth">
              <el-select v-model="ruleForm.type">
                <el-option label="括" value="include" />
                <el-option label="不包括" value="exclude" />
              </el-select>
            </el-form-item>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="dialogFormVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSave" style="--el-button-text-color: #000000">确定</el-button>
            </div>
          </template>
        </el-dialog>
      </VCard>
      
      <!-- 修改保留日志时间卡片 -->
      <VCard class="m-0 flex-1 md:m-4 mt-4" title="日志保留时间">
        <div class="flex items-center gap-4">
          <el-input-number 
            v-model="retentionDays" 
            :min="1" 
            :max="365"
            placeholder="请输入天数"
            style="width: 180px"
          />
          <span class="text-gray-600">天</span>
          <el-button 
            type="primary" 
            @click="saveRetentionDays"
            :loading="isSavingRetention"
            style="--el-button-text-color: #000000"
          >
            保存
          </el-button>
          <el-button 
            type="warning" 
            @click="handleCleanLogs"
            :loading="isCleaningLogs"
            style="--el-button-text-color: #000000"
          >
            立即清理
          </el-button>
        </div>
        <div class="mt-2 text-gray-500 text-sm">
          设置接口日志的保留时间，超过设定时间的日志将被自动清理。点击"立即清理"可以立即清理所有日志。
        </div>
      </VCard>
      
      <!-- 统计图卡组 -->
      <div class="flex justify-start items-stretch gap-4 md:m-4">
        <!-- 存储空间 -->
        <VCard class="w-13" title="存储空间">
          <div class="p-4 flex items-center justify-start" >
            <el-progress 
              type="dashboard" 
              :percentage="storageStats.usagePercentage" 
              :color="getProgressColor"
            >
              <template #default="{ percentage }">
                <div class="progress-content">
                  <div>{{ percentage }}%</div>
                  <div class="text-sm text-gray-500">
                    已用：{{ formatSize(storageStats.usedStorage) }}/{{ formatSize(storageStats.totalStorage) }}
                  </div>
                </div>
              </template>
            </el-progress>
          </div>
        </VCard>
        
        <!-- 内存使用 -->
        <VCard class="w-13" title="内存使用">
          <div class="p-4 flex items-center justify-start">
            <el-progress 
              type="dashboard" 
              :percentage="memoryStats.usagePercentage"
              :color="getProgressColor"
            >
              <template #default="{ percentage }">
                <div class="progress-content">
                  <div>{{ percentage }}%</div>
                  <div class="text-sm text-gray-500">
                    已用：{{ formatSize(memoryStats.usedMemory) }}/{{ formatSize(memoryStats.totalMemory) }}
                  </div>
                </div>
              </template>
            </el-progress>
          </div>
        </VCard>
      </div>
    </div>
  </VCard>
  <el-dialog
    v-model="detailDialogVisible"
    title="接口详情"
    width="80%">
    <div v-if="currentDetail" style="max-height: 70vh; overflow-y: auto;">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="请求时间">{{ currentDetail.accessTime }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="请求IP">{{ currentDetail.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URI" class="break-all">{{ currentDetail.path }}</el-descriptions-item>
        <el-descriptions-item label="请求类型" style="width: 180px">{{ currentDetail.requestType }}</el-descriptions-item>
        <el-descriptions-item label="响应码">{{ currentDetail.responseStatus }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <JsonViewer
            v-if="currentDetail.requestParams"
            :value="parseJson(currentDetail.requestParams)"
            :expand-depth="2"
            copyable
            sort
          />
        </el-descriptions-item>
        <el-descriptions-item label="请求头">
          <JsonViewer
            v-if="currentDetail.requestHeader"
            :value="parseJson(currentDetail.requestHeader)"
            :expand-depth="2"
            copyable
            sort
          />
        </el-descriptions-item>
        <el-descriptions-item label="请求体">
          <JsonViewer
            v-if="currentDetail.requestBody"
            :value="parseJson(currentDetail.requestBody)"
            :expand-depth="2"
            copyable
            sort
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应头">
          <JsonViewer
            v-if="currentDetail.responseHeader"
            :value="parseJson(currentDetail.responseHeader)"
            :expand-depth="2"
            copyable
            sort
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应内容">
          <JsonViewer
            v-if="currentDetail.responseBody"
            :value="parseJson(currentDetail.responseBody)"
            :expand-depth="2"
            copyable
            sort
          />
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </el-dialog>
  <!-- 删除确认对话框 -->
  <el-dialog
    v-model="deleteDialogVisible"
    title="删除确认"
    width="420px"
    center
    :close-on-click-modal="false"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item 
        label="规则内容" 
        label-class-name="rule-label"
        class="rule-content"
      >
        {{ deleteRow?.rule }}
      </el-descriptions-item>
      <el-descriptions-item 
        label="规类型"
        label-class-name="rule-label"
      >
        {{ deleteRow?.type === 'include' ? '包含' : '排除' }}
      </el-descriptions-item>
    </el-descriptions>
    <div class="delete-confirm-text">是否确认删该规则？</div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmDelete" 
          :loading="isLoading"
          style="color: black"
        >
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
  <!-- 新增规则对话框 -->
  <el-dialog 
    v-model="addDialogVisible" 
    title="新增规则" 
    width="500"
  >
    <el-form :model="newRuleForm">
      <el-form-item label="规则内容" :label-width="formLabelWidth">
        <el-input v-model="newRuleForm.rule" autocomplete="off" />
      </el-form-item>
      <el-form-item label="规则类型" :label-width="formLabelWidth">
        <el-select v-model="newRuleForm.type">
          <el-option label="包括" value="include" />
          <el-option label="不包括" value="exclude" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="saveNewRule" 
          :loading="isLoading"
          style="--el-button-text-color: #000000"
        >确定</el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 编辑规则对话框 -->
  <el-dialog 
    v-model="editDialogVisible" 
    title="编规" 
    width="500"
  >
    <el-form :model="editRuleForm">
      <el-form-item label="规则内容" :label-width="formLabelWidth">
        <el-input v-model="editRuleForm.rule" autocomplete="off" />
      </el-form-item>
      <el-form-item label="规则类型" :label-width="formLabelWidth">
        <el-select v-model="editRuleForm.type">
          <el-option label="包括" value="include" />
          <el-option label="不包括" value="exclude" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="updateRule" 
          :loading="isLoading"
          style="--el-button-text-color: #000000"
        >确定</el-button>
      </div>
    </template>
  </el-dialog>
  <!-- 清理确认对话框 -->
  <el-dialog
    v-model="cleanDialogVisible"
    title="清理确认"
    width="420px"
    center
    :close-on-click-modal="false"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item 
        label="待清理"
        label-class-name="rule-label"
      >
        {{ cleanStats.totalCount }} 条日志
      </el-descriptions-item>
    </el-descriptions>
    <div class="delete-confirm-text">是否确认清理这些日志？</div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cleanDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmClean" 
          :loading="isCleaningLogs"
          style="color: black"
        >
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>
<style lang="scss">
.date-picker {
  & input {
    border-radius: 0.375rem;
  }
}

.el-table .warning-row {
  background-color: oldlace;
}

.el-table .success-row {
  background-color: #f0f9eb;
}

.jv-container {
  max-height: inherit;
  overflow: auto;
  
  .jv-node {
    &.toggle {
      position: relative;
      padding-left: 15px;
      margin-left: -15px;
      
      &:before {
        position: absolute;
        left: 0;
      }
    }
  }
  
  .jv-code {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.jv-container.jv-light {
  background: none;
}

.el-descriptions__cell {
  &.is-label-top {
    min-width: 180px;
    width: 180px;
  }
}

.el-descriptions-item__content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 180px);
}

.jv-container {
  overflow: auto;
  max-height: 400px;
  border: 1px solid #ebeef5;
  padding: 10px;
  
  .jv-code {
    white-space: nowrap !important;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.el-descriptions-item__content {
  overflow-x: auto;
}

.el-table {
  .include-row {
    background-color: #f0f9eb;  // 绿背景
    &:hover > td {
      background-color: #e1f3d8 !important;
    }
  }
  
  .exclude-row {
    background-color: #fef0f0;  // 红色背景
    &:hover > td {
      background-color: #fde2e2 !important;
    }
  }
}

.delete-confirm-dialog {
  width: 420px;
}

.delete-confirm-dialog .el-message-box__header {
  padding: 15px;
  padding-bottom: 10px;
}

.delete-confirm-dialog .el-message-box__content {
  padding: 20px;
  padding-top: 10px;
}

.delete-confirm-dialog .el-message-box__btns {
  padding: 10px 15px 15px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.delete-confirm-text {
  margin-top: 16px;
  text-align: center;
  color: #606266;
}

.el-descriptions {
  margin-top: 8px;
}

.rule-label {
  width: 100px;
  text-align: right;
  padding-right: 12px;
}

.rule-content .el-descriptions-item__content {
  word-break: break-all;
}

.el-descriptions__cell {
  padding: 12px !important;
}

.el-button {
  &.el-button--small {
    height: 24px;
    line-height: 24px;
  }
}
html,body {
  padding-right: 0 !important;
}

.progress-content {
  text-align: center;
  line-height: 1.5;
}

.stat-item {
  padding: 12px;
  min-width: 120px;
}

:deep(.el-progress__text) {
  min-width: 120px !important;
}

.el-select {
  .el-input__inner {
    color: #000000 !important;
  }
  
  .el-select__input {
    color: #000000 !important;
  }
}

.VCard.w-13 {
    height: 50%; // 高度减半
    width: 50%;    // 宽度减半
    display: flex;
    align-items: center;
    justify-content: center; // 居中图表
}
</style>
