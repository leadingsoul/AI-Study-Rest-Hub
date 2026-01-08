<template>
    <div class="ai-guess-page">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="logo">
          <img src="../assets/logo.svg" alt="logo" class="logo-img" />
          <span class="title">AI猜词游戏</span>
        </div>
        <div class="nav-actions">
          <el-button @click="goToHome" icon="ArrowLeft">返回首页</el-button>
          <el-button type="primary" @click="goToGuessRecords" icon="Document">猜词记录</el-button>
        </div>
      </div>
  
      <!-- 主体内容区域 -->
      <div class="main-container">
        <!-- 左侧分类选择区域 -->
        <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
          <div class="sidebar-header">
            <h3>猜词分类</h3>
            <el-button 
              text 
              @click="toggleSidebar" 
              :icon="sidebarCollapsed ? 'Expand' : 'Fold'"
              class="toggle-btn"
            />
          </div>
          <div class="category-list" v-if="!sidebarCollapsed">
            <el-tree
              :data="categoryTree"
              :props="{ label: 'name', children: 'children' }"
              node-key="id"
              :expand-on-click-node="false"
              @node-click="handleCategoryClick"
              :default-expanded-keys="[0]"
            >
              <template #default="{ node, data }">
                <div class="category-node">
                  <span>{{ node.label }}</span>
                  <el-tag v-if="data.topicCount" size="small" type="info">{{ data.topicCount }}</el-tag>
                </div>
              </template>
            </el-tree>
          </div>
        </div>
  
        <!-- 中间对话区域 -->
        <div class="chat-container">
          <!-- 当前题目信息 -->
          <div class="topic-info" v-if="currentTopic">
            <div class="topic-header">
              <h2>{{ currentTopic.topicDescription }}</h2>
              <div class="topic-meta">
                <el-tag :type="getDifficultyType(currentTopic.difficulty)">
                  {{ getDifficultyText(currentTopic.difficulty) }}
                </el-tag>
                <span class="timer">
                  <el-icon><Timer /></el-icon>
                  {{ formatTime(duration) }}
                </span>
                <span class="chat-count">
                  <el-icon><ChatDotRound /></el-icon>
                  对话次数: {{ chatCount }}
                </span>
              </div>
            </div>
          </div>
  
          <!-- 对话历史区域 -->
          <div class="chat-history" ref="chatHistoryRef">
            <div 
              v-for="(message, index) in chatHistory" 
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
            <div v-if="loading" class="message-item ai-message">
              <div class="message-avatar">
                <el-avatar :size="40" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
  
          <!-- 输入区域 -->
          <div class="input-area">
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="请输入对话..."
              @keydown.enter.prevent="handleSend"
              :disabled="loading || !currentTopic"
            />
            <el-button 
              type="primary" 
              @click="handleSend"
              :loading="loading"
              :disabled="!userInput.trim() || !currentTopic"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
  
      <!-- 成功弹窗 -->
      <el-dialog
        v-model="successDialogVisible"
        title="猜词成功！"
        width="400px"
        :close-on-click-modal="false"
        :show-close="false"
      >
        <div class="success-content">
          <el-result
            icon="success"
            title="恭喜您猜对了！"
            :sub-title="`您用了 ${chatCount} 次对话，耗时 ${formatTime(duration)}`"
          />
          <div class="success-actions">
            <el-button @click="continueWithSameTopic">继续当前题目</el-button>
            <el-button type="primary" @click="selectNewTopic">选择新题目</el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, nextTick, watch, onBeforeUnmount } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { Timer, ChatDotRound, ArrowLeft, Fold, Expand } from '@element-plus/icons-vue'
  import request from '../utils/request'
  
  const router = useRouter()
  
  // 侧边栏状态
  const sidebarCollapsed = ref(false)
  
  // 当前题目信息
  const currentTopic = ref(null)
  
  // 分类树
  const categoryTree = ref([])
  
  // 对话历史
  const chatHistory = ref([])
  
  // 用户输入
  const userInput = ref('')
  
  // 加载状态
  const loading = ref(false)
  
  // 计时器
  const duration = ref(0)
  let timer = null
  
  //组件是否已挂载
  const isMounted = ref(false)

  // 对话次数
  const chatCount = ref(0)
  
  // 成功弹窗
  const successDialogVisible = ref(false)
  
  // 聊天历史区域引用
  const chatHistoryRef = ref(null)
  
  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  // 当前游戏标识
  const currentGameId = ref('')

  // 返回首页
  const goToHome = () => {
    router.push('/home')
  }
  // 顶级分类编码（用于传递给后端）
  const topCategoryCode = ref('')

  // 题目目标答案（需要猜测的内容）
  const targetAnswer = ref('')
  
  // 查看猜词记录
  const goToGuessRecords = () => {
    router.push('/ai-guess-record')
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
  
  // 格式化消息时间
  const formatMessageTime = (timestamp) => {
    const date = new Date(timestamp)
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  const buildCategoryTree = (flatList) => {
    // 创建ID到节点的映射
    const nodeMap = {}
    // 根节点列表
    const roots = []

    // 初始化节点映射
    flatList.forEach(item => {
      nodeMap[item.id] = {
        ...item,
        children: []
      }
    })

    // 构建树形结构
    flatList.forEach(item => {
      const node = nodeMap[item.id]
      // parent_id为0或null的是根节点
      if (item.parent_id === 0 || item.parent_id === null) {
        roots.push(node)
      } else {
        // 找到父节点并添加子节点
        const parentNode = nodeMap[item.parent_id]
        if (parentNode) {
          parentNode.children.push(node)
        } else {
          // 如果父节点不存在，作为根节点处理
          roots.push(node)
        }
      }
    })

    // 按sort_order排序
    const sortByOrder = (nodes) => {
      return nodes.sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0)).map(node => {
        node.children = sortByOrder(node.children)
        return node
      })
    }

    return sortByOrder(roots)
  }

  // 获取分类树
  const getCategoryTree = async () => {
    try {
      const res = await request.get('/api/guess/category/tree')
      if(isMounted.value){
        const treeData = buildCategoryTree(res.data || [])
        categoryTree.value = treeData
      }
    } catch (error) {
      console.error('获取分类树失败：', error)
      if(isMounted.value){
        ElMessage.error('获取分类树失败')
      }
    }
  }
  
  // 处理分类点击
  const handleCategoryClick = async (data) => {
    if (!data.id) return
    
    try {
      // 获取该分类下的随机题目
      const res = await request.get(`/api/guess/topic/random?categoryId=${data.id}`)
      if (res.data&&isMounted.value) {
        topCategoryCode.value = res.data.topCategoryCode || 'DEFAULT'
        targetAnswer.value = res.data.target || ''
        startNewTopic(res.data)
      } else if(isMounted.value) {
        ElMessage.warning('该分类下暂无题目')
      }
    } catch (error) {
      console.error('获取题目失败：', error)
      if(isMounted.value){
        ElMessage.error('获取题目失败')
      }
    }
  }
  
  // 开始新题目
  const startNewTopic = (topic) => {
    if(!isMounted.value) return

    // 生成新游戏标识
    currentGameId.value = `${Date.now()}-${Math.floor(Math.random() * 10000)}`

    // 重置状态
    currentTopic.value = topic
    chatHistory.value = []
    userInput.value = ''
    chatCount.value = 0
    duration.value = 0
    
    // 清除旧计时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    
    // 启动新计时器
    timer = setInterval(() => {
        if(isMounted.value){
          duration.value++
        }
        else{
          clearInterval(timer)
        }
    }, 1000)
    
    // 添加欢迎消息
    chatHistory.value.push({
      role: 'assistant',
      content: `(请输入对话开始游戏)`,
      timestamp: Date.now(),
      gameId: currentGameId.value
    })
    
    // 滚动到底部
    nextTick(() => {
       if (isMounted.value && chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
    }
    })
  }
  
  // 发送消息
  const handleSend = async () => {
    if (!userInput.value.trim() || loading.value || !currentTopic.value) return
    
    const userMessage = userInput.value.trim()
    userInput.value = ''
    
    //校验游戏ID（防止跨局）
    if (chatHistory.value.length > 0 && chatHistory.value[0].gameId !== currentGameId.value) {
      ElMessage.warning('当前游戏已失效，请重新选择题目')
      return
    }
    if(chatHistory.value.length > 50){
      chatHistory.value = chatHistory.value.slice(-50)
    }
    // 添加用户消息
    chatHistory.value.push({
      role: 'user',
      content: userMessage,
      timestamp: Date.now(),
      gameId: currentGameId.value
    })
    
    // 增加对话次数
    chatCount.value++
    
    // 滚动到底部
    nextTick(() => {
      if (isMounted.value && chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
    }
    })
    
    // 设置加载状态
    loading.value = true
    
    try {
      // 发送消息到后端（修改请求参数）
      const res = await request.post('/api/guess/chat', chatHistory.value.slice(0, -1), {
        params: {
          topicId: currentTopic.value.id,
          message: userMessage,
          topCategoryCode: topCategoryCode.value,
          target: targetAnswer.value
        }
      })
      
      if(!isMounted.value) return

      // 安全地获取响应数据
      const responseData = res.data?.data || res.data || {}
      const aiContent = responseData.content || '抱歉，AI暂时无法回复，请稍后再试。'
      const isCorrect = responseData.isCorrect || false

      // 添加AI回复
      chatHistory.value.push({
        role: 'assistant',
        content: aiContent,
        timestamp: Date.now()
      })
      
      // 检查是否猜对了（后端返回isCorrect标识）
      if (isCorrect) {
        // 清除计时器
        if (timer) {
          clearInterval(timer)
          timer = null
        }
        
        // 保存猜词记录
        //await saveGuessRecord(true)
        
        // 显示成功弹窗
        successDialogVisible.value = true
      }
    } catch (error) {
    console.error('发送消息失败：', error)
    if (isMounted.value) {
      ElMessage.error('发送消息失败')
    }
  } finally {
    if (isMounted.value) {
      loading.value = false
      
      // 滚动到底部
      nextTick(() => {
        if (isMounted.value && chatHistoryRef.value) {
          chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
        }
      })
    }
  }
}
  
  // 滚动到底部
  const scrollToBottom = () => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
    }
  }
  
  // 保存猜词记录
  const saveGuessRecord = async (isSuccess) => {
    try {
      await request.post('/api/guess/record/save', {
        topicId: currentTopic.value.id,
        guessCount: chatCount.value,
        successCount: isSuccess ? 1 : 0,
        status: isSuccess ? 1 : 0,
        duration: duration.value
      })
    } catch (error) {
      console.error('保存猜词记录失败：', error)
    }
  }
  
  // 继续当前题目
  const continueWithSameTopic = () => {
    if(!isMounted.value) return
    successDialogVisible.value = false
    
    // 重置对话状态但保留题目和分类信息
    chatHistory.value = []
    userInput.value = ''
    chatCount.value = 0
    duration.value = 0
    
    // 重新启动计时器
    timer = setInterval(() => {
        if(isMounted.value){
          duration.value++
        }
        else{
          clearInterval(timer)
        }
    }, 1000)
    
    // 添加欢迎消息
    chatHistory.value.push({
      role: 'assistant',
      content: `让我们继续"${currentTopic.value.topicName}"！请问您有什么问题？`,
      timestamp: Date.now()
    })
    
    // 滚动到底部
    nextTick(() => {
      if(isMounted.value){
        scrollToBottom()
      }
    })
}
  
  // 选择新题目
  const selectNewTopic = () => {
    if(!isMounted.value) return
    successDialogVisible.value = false
    currentTopic.value = null
    chatHistory.value = []
    topCategoryCode.value = ''
    targetAnswer.value = ''
    
    // 清除计时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }
  
  // 监听聊天历史变化，自动滚动到底部
  watch(chatHistory, () => {
    nextTick(() => {
      if(isMounted.value){
        scrollToBottom()
      }
    })
  }, { deep: true })
  
  // 初始化
  onMounted(() => {
    isMounted.value = true
    getCategoryTree()
  })
  
  // 组件卸载时清除计时器
  onBeforeUnmount(() => {
    isMounted.value = false
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    //清空所有局相关状态
    chatHistory.value = []
    currentTopic.value = null
    currentGameId.value = ''
    duration.value = 0
    chatCount.value = 0
  })
  </script>
  
  <style scoped>
  .ai-guess-page {
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
    display: flex;
    flex: 1;
    overflow: hidden;
  }
  
  /* 侧边栏样式 */
  .sidebar {
    width: 250px;
    background: white;
    box-shadow: 2px 0 8px rgba(0,0,0,0.1);
    transition: width 0.3s ease;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  .sidebar.collapsed {
    width: 60px;
  }
  
  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #eee;
  }
  
  .sidebar-header h3 {
    margin: 0;
    font-size: 1.2rem;
    color: #333;
  }
  
  .toggle-btn {
    flex-shrink: 0;
  }
  
  .category-list {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
  
  .category-node {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  /* 对话容器样式 */
  .chat-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin: 20px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 10px 40px rgba(0,0,0,0.1);
    overflow: hidden;
  }
  
  /* 题目信息样式 */
  .topic-info {
    padding: 20px;
    border-bottom: 1px solid #eee;
    background: linear-gradient(45deg, #f8f9fa, #e9ecef);
  }
  
  .topic-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .topic-header h2 {
    margin: 0;
    color: #333;
  }
  
  .topic-meta {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .timer, .chat-count {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #666;
  }
  
  .topic-description {
    color: #666;
    line-height: 1.6;
  }
  
  /* 对话历史样式 */
  .chat-history {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .message-item {
    display: flex;
    gap: 12px;
    max-width: 80%;
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
    background: #f5f5f5;
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
  
  /* 输入动画效果 */
  .typing-indicator {
    display: flex;
    gap: 4px;
    padding: 12px 16px;
    background: #f5f5f5;
    border-radius: 16px;
  }
  
  .typing-indicator span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #999;
    animation: typing 1.4s infinite;
  }
  
  .typing-indicator span:nth-child(2) {
    animation-delay: 0.2s;
  }
  
  .typing-indicator span:nth-child(3) {
    animation-delay: 0.4s;
  }
  
  @keyframes typing {
    0%, 60%, 100% {
      transform: translateY(0);
    }
    30% {
      transform: translateY(-10px);
    }
  }
  
  /* 输入区域样式 */
  .input-area {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #eee;
  }
  
  .input-area .el-textarea {
    flex: 1;
  }
  
  /* 成功弹窗样式 */
  .success-content {
    text-align: center;
  }
  
  .success-actions {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    gap: 12px;
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
      margin: 10px;
    }
    
    .sidebar {
      width: 200px;
    }
    
    .sidebar.collapsed {
      width: 50px;
    }
    
    .message-item {
      max-width: 90%;
    }
  }
  </style>