<template>
  <div class="guess-manage-container">
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="manage-tabs">
      <!-- 猜词分类管理 -->
      <el-tab-pane label="猜词分类管理" name="category">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button type="primary" @click="showAddCategoryDialog" icon="Plus">添加分类</el-button>
            <el-button @click="getCategoryTree" icon="Refresh">刷新列表</el-button>
          </div>

          <!-- 分类表格 -->
          <div class="category-list">
            <el-table
              :data="categoryList"
              v-loading="categoryLoading"
              row-key="id"
              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
              stripe
            >
              <el-table-column prop="name" label="分类名称" min-width="200">
                <template #default="{ row }">
                  <span>{{ row.name }}</span>
                  <el-tag v-if="row.parentId === 0" size="small" type="primary" style="margin-left: 8px;">顶级分类</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="editCategory(row)" icon="Edit">编辑</el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteCategory(row)" 
                    icon="Delete"
                    :disabled="row.children && row.children.length > 0"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- 猜词题目管理 -->
      <el-tab-pane label="猜词题目管理" name="topic">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button type="primary" @click="showAddTopicDialog" icon="Plus">添加题目</el-button>
            <el-button @click="getTopicList" icon="Refresh">刷新列表</el-button>
            <el-select
              v-model="topicFilter.categoryId"
              placeholder="选择分类"
              clearable
              style="width: 150px; margin-left: 10px;"
              @change="handleTopicFilter"
            >
              <el-option
                v-for="category in flatCategoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
            <el-select
              v-model="topicFilter.difficulty"
              placeholder="选择难度"
              clearable
              style="width: 120px; margin-left: 10px;"
              @change="handleTopicFilter"
            >
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
            <el-select
              v-model="topicFilter.topCategoryCode"
              placeholder="顶级分类"
              clearable
              style="width: 150px; margin-left: 10px;"
              @change="handleTopicFilter"
            >
              <el-option label="猜病(DISEASE)" value="DISEASE" />
              <el-option label="猜物(ITEM)" value="ITEM" />
              <el-option label="猜人物(PERSON)" value="PERSON" />
              <el-option label="默认(DEFAULT)" value="DEFAULT" />
            </el-select>
            <el-input
              v-model="topicFilter.keyword"
              placeholder="搜索题目名称"
              @input="handleTopicFilter"
              style="width: 200px; margin-left: 10px;"
              clearable
            >
              <template #append>
                <el-button icon="Search" @click="handleTopicFilter" />
              </template>
            </el-input>
          </div>

          <!-- 题目表格 -->
          <div class="topic-list">
            <el-table :data="topicList" v-loading="topicLoading" stripe>
              <el-table-column prop="topicName" label="题目名称" min-width="200" />
              <el-table-column prop="categoryName" label="所属分类" width="150" />
              <el-table-column prop="difficulty" label="难度" width="100">
                <template #default="{ row }">
                  <el-tag :type="getDifficultyType(row.difficulty)">
                    {{ getDifficultyText(row.difficulty) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="topCategoryCode" label="顶级分类" width="120">
                <template #default="{ row }">
                  <el-tag :type="getTopCategoryType(row.topCategoryCode)">
                    {{ getTopCategoryText(row.topCategoryCode) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="target" label="目标词" width="150" show-overflow-tooltip />
              <el-table-column prop="recordCount" label="参与次数" width="100" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="editTopic(row)" icon="Edit">编辑</el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="deleteTopic(row)" 
                    icon="Delete"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="topicPagination.currentPage"
                v-model:page-size="topicPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="topicPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleTopicSizeChange"
                @current-change="handleTopicCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryForm.id ? '编辑分类' : '添加分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryFormRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父分类" style="width: 100%">
            <el-option label="顶级分类" :value="0" />
            <el-option
              v-for="category in flatCategoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
              :disabled="category.id === categoryForm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="categoryForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="categorySaveLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑题目对话框 -->
    <el-dialog
      v-model="topicDialogVisible"
      :title="topicForm.id ? '编辑题目' : '添加题目'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="topicFormRef"
        :model="topicForm"
        :rules="topicFormRules"
        label-width="100px"
      >
        <el-form-item label="题目名称" prop="topicName">
          <el-input v-model="topicForm.topicName" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="topicForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in flatCategoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="topicForm.difficulty" placeholder="请选择难度" style="width: 100%">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="顶级分类" prop="topCategoryCode">
        <el-select v-model="topicForm.topCategoryCode" placeholder="请选择顶级分类" style="width: 100%">
          <el-option label="猜病(DISEASE)" value="DISEASE" />
          <el-option label="猜物(ITEM)" value="ITEM" />
          <el-option label="猜人物(PERSON)" value="PERSON" />
          <el-option label="默认(DEFAULT)" value="DEFAULT" />
        </el-select>
      </el-form-item>
        <el-form-item label="目标词" prop="target">
          <el-input v-model="topicForm.target" placeholder="请输入目标词" />
        </el-form-item>
        <el-form-item label="题目描述" prop="topicDescription">
          <el-input
            v-model="topicForm.topicDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入题目描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTopic" :loading="topicSaveLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, ArrowLeft, MagicStick, Refresh } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()

// 当前激活的标签页
const activeTab = ref('category')

// 分类相关数据
const categoryList = ref([])
const categoryLoading = ref(false)
const categorySearchKeyword = ref('')
const categoryDialogVisible = ref(false)
const categorySaveLoading = ref(false)
const categoryFormRef = ref()
const categoryForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  description: '',
  sortOrder: 0,
  status: 1
})
const categoryFormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择父分类', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 题目相关数据
const topicList = ref([])
const topicLoading = ref(false)
const topicFilter = reactive({
  categoryId: null,
  difficulty: null,
  topCategoryCode: null,
  keyword: ''
})
const topicPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const topicDialogVisible = ref(false)
const topicSaveLoading = ref(false)
const aiGenerating = ref(false)
const topicFormRef = ref()
const topicForm = reactive({
  id: null,
  topicName: '',
  categoryId: null,
  difficulty: 'MEDIUM',
  topCategoryCode: 'DEFAULT',
  target: '',
  topicDescription: ''
})
const topicFormRules = {
  topicName: [{ required: true, message: '请输入题目名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  topCategoryCode: [{ required: true, message: '请选择顶级分类', trigger: 'change' }],
  target: [{ required: true, message: '请输入目标词', trigger: 'blur' }]
}

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

// 返回首页
const goToHome = () => {
  router.push('/home')
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

// 新增：获取顶级分类标签样式
const getTopCategoryType = (topCategoryCode) => {
  const typeMap = {
    'DISEASE': 'danger',
    'ITEM': 'primary',
    'PERSON': 'success',
    'DEFAULT': 'info'
  }
  return typeMap[topCategoryCode] || 'info'
}

// 新增：获取顶级分类文本
const getTopCategoryText = (topCategoryCode) => {
  const textMap = {
    'DISEASE': '猜病',
    'ITEM': '猜物',
    'PERSON': '猜人物',
    'DEFAULT': '默认'
  }
  return textMap[topCategoryCode] || topCategoryCode
}

// 获取分类树
const getCategoryTree = async () => {
  categoryLoading.value = true
  try {
    const res = await request.get('/api/guess/category/tree')
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类树失败：', error)
    ElMessage.error('获取分类树失败')
  } finally {
    categoryLoading.value = false
  }
}

// 搜索分类
// const handleCategorySearch = () => {
//   // 这里可以实现搜索逻辑
//   getCategoryTree()
// }

// 显示添加分类对话框
const showAddCategoryDialog = () => {
  resetCategoryForm()
  categoryDialogVisible.value = true
}

// 编辑分类
const editCategory = (category) => {
  Object.assign(categoryForm, category)
  categoryDialogVisible.value = true
}

// 删除分类
const deleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类"${category.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.post(`/api/guess/category/delete/${category.id}`)
    ElMessage.success('删除成功')
    getCategoryTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败：', error)
      ElMessage.error('删除分类失败')
    }
  }
}

// 重置分类表单
const resetCategoryForm = () => {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.parentId = 0
  categoryForm.description = ''
  categoryForm.sortOrder = 0
  categoryForm.status = 1
}

// 保存分类
const saveCategory = async () => {
  if (!categoryFormRef.value) return
  
  await categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      categorySaveLoading.value = true
      try {
        if (categoryForm.id) {
          await request.post(`/api/guess/category/update/${categoryForm.id}`, categoryForm)
          ElMessage.success('更新成功')
        } else {
          await request.post('/api/guess/category/save', categoryForm)
          ElMessage.success('添加成功')
        }
        categoryDialogVisible.value = false
        getCategoryTree()
      } catch (error) {
        console.error('保存分类失败：', error)
        ElMessage.error('保存分类失败')
      } finally {
        categorySaveLoading.value = false
      }
    }
  })
}

// 获取题目列表
const getTopicList = async () => {
  topicLoading.value = true
  try {
    const params = {
      page: topicPagination.currentPage,
      size: topicPagination.pageSize,
      categoryId: topicFilter.categoryId,
      difficulty: topicFilter.difficulty,
      topCategoryCode: topicFilter.topCategoryCode,
      keyword: topicFilter.keyword
    }
    const res = await request.get('/api/guess/topic', { params })
    topicList.value = res.data.records || []
    topicPagination.total = res.data.total || 0
  } catch (error) {
    console.error('获取题目列表失败：', error)
    ElMessage.error('获取题目列表失败')
  } finally {
    topicLoading.value = false
  }
}

// 处理题目筛选
const handleTopicFilter = () => {
  topicPagination.currentPage = 1
  getTopicList()
}

// 处理题目分页大小变化
const handleTopicSizeChange = (size) => {
  topicPagination.pageSize = size
  getTopicList()
}

// 处理题目当前页变化
const handleTopicCurrentChange = (page) => {
  topicPagination.currentPage = page
  getTopicList()
}

// 显示添加题目对话框
const showAddTopicDialog = () => {
  resetTopicForm()
  topicDialogVisible.value = true
}

// 编辑题目
const editTopic = (topic) => {
  Object.assign(topicForm, topic)
  topicDialogVisible.value = true
}

// 删除题目
const deleteTopic = async (topic) => {
  try {
    await ElMessageBox.confirm(`确定要删除题目"${topic.topicName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.post(`/api/guess/topic/delete/${topic.id}`)
    ElMessage.success('删除成功')
    getTopicList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除题目失败：', error)
      ElMessage.error('删除题目失败')
    }
  }
}

// 重置题目表单
const resetTopicForm = () => {
  topicForm.id = null
  topicForm.topicName = ''
  topicForm.categoryId = null
  topicForm.difficulty = 'MEDIUM'
  topicForm.topCategoryCode = 'DEFAULT'
  topicForm.target = ''
  topicForm.topicDescription = ''
}

// // AI生成题目
// const generateTopicByAI = async () => {
//   if (!topicForm.categoryId) {
//     ElMessage.warning('请先选择分类')
//     return
//   }
  
//   aiGenerating.value = true
//   try {
//     const res = await request.post('/api/guess/topic/ai-generate', {
//       categoryId: topicForm.categoryId,
//       difficulty: topicForm.difficulty
//     })
    
//     if (res.data) {
//       Object.assign(topicForm, res.data)
//       ElMessage.success('AI生成题目成功')
//     }
//   } catch (error) {
//     console.error('AI生成题目失败：', error)
//     ElMessage.error('AI生成题目失败')
//   } finally {
//     aiGenerating.value = false
//   }
// }

// 保存题目
const saveTopic = async () => {
  if (!topicFormRef.value) return
  
  await topicFormRef.value.validate(async (valid) => {
    if (valid) {
      topicSaveLoading.value = true
      try {
        if (topicForm.id) {
          await request.post(`/api/guess/topic/update/${topicForm.id}`, topicForm)
          ElMessage.success('更新成功')
        } else {
          await request.post('/api/guess/topic/save', topicForm)
          ElMessage.success('添加成功')
        }
        topicDialogVisible.value = false
        getTopicList()
      } catch (error) {
        console.error('保存题目失败：', error)
        ElMessage.error('保存题目失败')
      } finally {
        topicSaveLoading.value = false
      }
    }
  })
}

onMounted(() => {
  getCategoryTree()
  getTopicList()
})
</script>

<style scoped>
.guess-manage-container { padding: 20px; }
.action-bar { margin-bottom: 20px; }
.category-list, .topic-list { 
  background: white; 
  border-radius: 8px; 
  overflow: hidden; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.1); 
  padding: 20px;
}
.pagination { 
  margin-top: 20px; 
  text-align: right; 
}
.tab-content { padding: 0; }
</style>