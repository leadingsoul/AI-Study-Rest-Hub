<template>
    <div class="guess-records-page">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="logo">
          <img src="../assets/logo.svg" alt="logo" class="logo-img" />
          <span class="title">猜词记录</span>
        </div>
        <div class="nav-actions">
          <el-button @click="goToHome" icon="ArrowLeft">返回首页</el-button>
          <el-button type="primary" @click="goToAiGuess" icon="Flag">开始猜词</el-button>
        </div>
      </div>
  
      <!-- 主体内容区域 -->
      <div class="main-container">
        <!-- 统计卡片 -->
        <div class="stats-cards">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon class="stat-icon total-icon"><Document /></el-icon>
              <div class="stat-text">
                <div class="stat-number">{{ stats.totalCount || 0 }}</div>
                <div class="stat-label">总猜词次数</div>
              </div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon class="stat-icon success-icon"><SuccessFilled /></el-icon>
              <div class="stat-text">
                <div class="stat-number">{{ stats.successCount || 0 }}</div>
                <div class="stat-label">成功次数</div>
              </div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon class="stat-icon rate-icon"><TrendCharts /></el-icon>
              <div class="stat-text">
                <div class="stat-number">{{ stats.successRate || 0 }}%</div>
                <div class="stat-label">成功率</div>
              </div>
            </div>
          </el-card>
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon class="stat-icon time-icon"><Timer /></el-icon>
              <div class="stat-text">
                <div class="stat-number">{{ formatTime(stats.avgDuration || 0) }}</div>
                <div class="stat-label">平均用时</div>
              </div>
            </div>
          </el-card>
        </div>
  
        <!-- 筛选区域 -->
        <el-card class="filter-card" shadow="never">
          <el-form :model="filterForm" inline>
            <el-form-item label="题目名称">
              <el-input
                v-model="filterForm.topicName"
                placeholder="请输入题目名称"
                clearable
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select
                v-model="filterForm.categoryId"
                placeholder="请选择分类"
                clearable
                style="width: 150px"
              >
                <el-option
                  v-for="category in flatCategoryList"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="难度">
              <el-select
                v-model="filterForm.difficulty"
                placeholder="请选择难度"
                clearable
                style="width: 120px"
              >
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="filterForm.status"
                placeholder="请选择状态"
                clearable
                style="width: 120px"
              >
                <el-option label="成功" :value="1" />
                <el-option label="未完成" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFilter" icon="Search">查询</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
  
        <!-- 记录表格 -->
        <el-card class="table-card" shadow="never">
          <el-table :data="recordList" v-loading="loading" style="width: 100%">
            <el-table-column prop="topicName" label="题目名称" min-width="200" />
            <el-table-column prop="categoryName" label="所属分类" width="150" />
            <el-table-column prop="difficulty" label="难度" width="100">
              <template #default="{ row }">
                <el-tag :type="getDifficultyType(row.difficulty)">
                  {{ getDifficultyText(row.difficulty) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="target" label="目标词" width="150" show-overflow-tooltip />
            <el-table-column prop="guessCount" label="猜词次数" width="100" />
            <el-table-column prop="successCount" label="成功次数" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                  {{ row.status === 1 ? '成功' : '未完成' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="用时" width="100">
              <template #default="{ row }">
                {{ formatTime(row.duration) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="记录时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="viewRecordDetail(row)" icon="View">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
  
          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </div>
  
      <!-- 记录详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="猜词记录详情"
        width="800px"
        :close-on-click-modal="false"
      >
        <div class="record-detail" v-if="currentRecord">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="题目名称">{{ currentRecord.topicName }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ currentRecord.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="难度">
              <el-tag :type="getDifficultyType(currentRecord.difficulty)">
                {{ getDifficultyText(currentRecord.difficulty) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="目标词">{{ currentRecord.target }}</el-descriptions-item>
            <el-descriptions-item label="猜词次数">{{ currentRecord.guessCount }}</el-descriptions-item>
            <el-descriptions-item label="成功次数">{{ currentRecord.successCount }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentRecord.status === 1 ? 'success' : 'warning'">
                {{ currentRecord.status === 1 ? '成功' : '未完成' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用时">{{ formatTime(currentRecord.duration) }}</el-descriptions-item>
            <el-descriptions-item label="记录时间" span="2">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
          </el-descriptions>
  
          <div class="chat-history" v-if="currentRecord.chatHistory && currentRecord.chatHistory.length > 0">
            <h3>对话记录</h3>
            <div class="chat-container">
              <div 
                v-for="(message, index) in currentRecord.chatHistory" 
                :key="index" 
                class="message-item"
                :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
              >
                <div class="message-avatar">
                  <el-avatar v-if="message.role === 'user'" :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                  <el-avatar v-else :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                </div>
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatMessageTime(message.timestamp) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { 
    Document, SuccessFilled, TrendCharts, Timer, 
    ArrowLeft, Flag, Search, View 
  } from '@element-plus/icons-vue'
  import request from '../utils/request'
  
  const router = useRouter()
  
  // 统计数据
  const stats = ref({
    totalCount: 0,
    successCount: 0,
    successRate: 0,
    avgDuration: 0
  })
  
  // 分类列表
  const categoryList = ref([])
  
  // 扁平化分类列表
  const flatCategoryList = computed(() => {
    const flatten = (categories, result = []) => {
      categories.forEach(category => {
        result.push(category)
        if (category.children && category.children.length > 0) {
          flatten(category.children, result)
        }
      })
      return result
    }
    return flatten(categoryList.value)
  })
  
  // 筛选表单
  const filterForm = reactive({
    topicName: '',
    categoryId: null,
    difficulty: null,
    status: null,
    dateRange: []
  })
  
  // 记录列表
  const recordList = ref([])
  const loading = ref(false)
  
  // 分页
  const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
  })
  
  // 记录详情
  const detailDialogVisible = ref(false)
  const currentRecord = ref(null)
  
  // 返回首页
  const goToHome = () => {
    router.push('/home')
  }
  
  // 前往猜词界面
  const goToAiGuess = () => {
    router.push('/ai-guess')
  }
  
  // 获取难度标签样式
  const getDifficultyType = (difficulty) => {
    const typeMap = {
      'EASY': 'success',
      'MEDIUM': 'warning',
      'HARD': 'danger'
    }
    return typeMap[difficulty] || 'info'
  }
  
  // 获取难度文本
  const getDifficultyText = (difficulty) => {
    const textMap = {
      'EASY': '简单',
      'MEDIUM': '中等',
      'HARD': '困难'
    }
    return textMap[difficulty] || difficulty
  }
  
  // 格式化时间
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }
  
  // 格式化日期时间
  const formatDateTime = (timestamp) => {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 格式化消息时间
  const formatMessageTime = (timestamp) => {
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 获取统计数据
  const getStats = async () => {
    try {
      const res = await request.get('/api/guess/record/stats')
      stats.value = res.data || {}
    } catch (error) {
      console.error('获取统计数据失败：', error)
      ElMessage.error('获取统计数据失败')
    }
  }
  
  // 获取分类树
  const getCategoryTree = async () => {
    try {
      const res = await request.get('/api/guess/category/tree')
      categoryList.value = res.data || []
    } catch (error) {
      console.error('获取分类树失败：', error)
      ElMessage.error('获取分类树失败')
    }
  }
  
  // 获取记录列表
  const getRecordList = async () => {
    loading.value = true
    try {
      const params = {
        page: pagination.currentPage,
        size: pagination.pageSize,
        topicName: filterForm.topicName,
        categoryId: filterForm.categoryId,
        difficulty: filterForm.difficulty,
        status: filterForm.status,
        startDate: filterForm.dateRange && filterForm.dateRange.length > 0 ? filterForm.dateRange[0] : null,
        endDate: filterForm.dateRange && filterForm.dateRange.length > 1 ? filterForm.dateRange[1] : null
      }
      const res = await request.get('/api/guess/record', { params })
      recordList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } catch (error) {
      console.error('获取记录列表失败：', error)
      ElMessage.error('获取记录列表失败')
    } finally {
      loading.value = false
    }
  }
  
  // 处理筛选
  const handleFilter = () => {
    pagination.currentPage = 1
    getRecordList()
  }
  
  // 重置筛选
  const resetFilter = () => {
    filterForm.topicName = ''
    filterForm.categoryId = null
    filterForm.difficulty = null
    filterForm.status = null
    filterForm.dateRange = []
    handleFilter()
  }
  
  // 处理分页大小变化
  const handleSizeChange = (size) => {
    pagination.pageSize = size
    getRecordList()
  }
  
  // 处理当前页变化
  const handleCurrentChange = (page) => {
    pagination.currentPage = page
    getRecordList()
  }
  
  // 查看记录详情
  const viewRecordDetail = async (record) => {
    try {
      const res = await request.get(`/api/guess/record/${record.id}`)
      currentRecord.value = res.data
      detailDialogVisible.value = true
    } catch (error) {
      console.error('获取记录详情失败：', error)
      ElMessage.error('获取记录详情失败')
    }
  }
  
  // 初始化
  onMounted(() => {
    getStats()
    getCategoryTree()
    getRecordList()
  })
  </script>
  
  <style scoped>
  .guess-records-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
    display: flex;
    flex-direction: column;
  }
  
  /* 导航栏样式 */
  .navbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    padding: 0 40px;
    height: 64px;
    box-shadow: 0 2px 20px rgba(0,0,0,0.1);
    position: sticky;
    top: 0;
    z-index: 100;
  }
  
  .logo {
    display: flex;
    align-items: center;
  }
  
  .logo-img {
    width: 36px;
    height: 36px;
    margin-right: 12px;
  }
  
  .title {
    font-size: 1.5rem;
    font-weight: bold;
    background: linear-gradient(45deg, #4cc9f0,#4361ee);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .nav-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  /* 主容器 */
  .main-container {
    flex: 1;
    padding: 20px;
    max-width: 1400px;
    margin: 0 auto;
    width: 100%;
  }
  
  /* 统计卡片样式 */
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .stat-card {
    border-radius: 12px;
    overflow: hidden;
  }
  
  .stat-content {
    display: flex;
    align-items: center;
  }
  
  .stat-icon {
    font-size: 40px;
    margin-right: 15px;
    padding: 10px;
    border-radius: 50%;
    color: white;
  }
  
  .total-icon { background-color: #409EFF; }
  .success-icon { background-color: #67C23A; }
  .rate-icon { background-color: #E6A23C; }
  .time-icon { background-color: #F56C6C; }
  
  .stat-text {
    flex: 1;
  }
  
  .stat-number {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 4px;
  }
  
  /* 筛选卡片样式 */
  .filter-card {
    margin-bottom: 20px;
    border-radius: 12px;
  }
  
  /* 表格卡片样式 */
  .table-card {
    border-radius: 12px;
  }
  
  /* 分页容器 */
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
  
  /* 记录详情样式 */
  .record-detail {
    max-height: 70vh;
    overflow-y: auto;
  }
  
  .chat-history {
    margin-top: 20px;
  }
  
  .chat-history h3 {
    margin-bottom: 16px;
    color: #303133;
  }
  
  .chat-container {
    border: 1px solid #EBEEF5;
    border-radius: 4px;
    padding: 16px;
    max-height: 400px;
    overflow-y: auto;
    background-color: #F5F7FA;
  }
  
  .message-item {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .user-message {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
  
  .ai-message {
    align-self: flex-start;
  }
  
  .message-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    max-width: 70%;
  }
  
  .message-text {
    padding: 12px 16px;
    border-radius: 16px;
    line-height: 1.5;
    word-wrap: break-word;
  }
  
  .user-message .message-text {
    background: #409eff;
    color: white;
  }
  
  .ai-message .message-text {
    background: white;
    color: #333;
  }
  
  .message-time {
    font-size: 0.75rem;
    color: #999;
    align-self: flex-end;
  }
  
  .user-message .message-time {
    align-self: flex-start;
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .navbar {
      padding: 0 20px;
    }
    
    .nav-actions {
      gap: 8px;
    }
    
    .nav-actions .el-button span {
      display: none;
    }
    
    .main-container {
      padding: 10px;
    }
    
    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
      gap: 10px;
    }
    
    .filter-card .el-form {
      flex-direction: column;
    }
    
    .filter-card .el-form-item {
      margin-right: 0;
      margin-bottom: 10px;
    }
  }
  </style>