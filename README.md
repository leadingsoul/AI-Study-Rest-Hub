# AI Study Rest Hub

一个“学习 + 休闲”一体化项目，包含前端（`front-end`）和 Spring Boot 后端服务（`AI_study_rest_hub_server`）。系统围绕题库练习、试卷考试、视频学习、公告轮播、AI 猜词等场景提供完整能力。

## 项目结构

```text
AI-Study-Rest-Hub/
├─ front-end                   # 前端（Vue 项目）
└─ AI_Study-Rest_Hub/AI_study_rest_hub_server/  # 后端服务（Spring Boot + MyBatis-Plus）
```

## 一、前端（`front-end`）

`front-end` 目录为需要运行的资源，未完成构建

页面/模块可从打包文件名识别，主要包括：

- 学习与考试：`QuestionManage`、`CategoryManage`、`PaperManage`、`PaperCreate`、`Exam`、`ExamStart`、`ExamResult`、`ExamRanking`
- 视频学习：`VideoList`、`VideoDetail`、`VideoManage`、`VideoCategoryManage`
- 系统运营：`BannerManage`、`NoticeManage`、`ScoreManage`、`Stats/Welcome/Home`
- AI 猜词：`AiGuess`、`AiGuessManage`、`AiGuessRecord`

### 前端请求特征

从 `assets/request-*.js` 可确认：

- 使用 Axios 统一封装请求
- 默认后端地址：`http://localhost:8080`
- 请求头自动携带 `Bearer Token`（从 `localStorage.userInfo.token` 读取）
- 期望后端统一响应结构：`code === 200` 视为成功

## 二、后端（`AI_study_rest_hub_server`）
## 技术栈

- Java 17
- Spring Boot 3.4.x（Web、Validation、Actuator、Cache、Redis）
- MyBatis-Plus 3.5.x
- MySQL
- JWT（`jjwt`）
- Spring AI Alibaba（DashScope + Agent Framework）
- MinIO（对象存储）
- Knife4j / SpringDoc（接口文档）
- Apache POI（Excel 导入处理）
- Redisson / Caffeine（缓存与并发能力）

## 核心业务模块（按 Controller 分组）
- 用户认证：`/api/user/**`
  - 登录、登出、管理员权限校验
- 题库管理：`/api/categories/**`、`/api/questions/**`
  - 分类树、题目 CRUD、随机题、热门题
- 批量题目处理：`/api/questions/batch/**`
  - 模板下载、Excel 预览、AI 生成、校验、批量导入
- 试卷与考试：`/api/papers/**`、`/api/exams/**`、`/api/exam-records/**`
  - 组卷（含 AI）、开考、交卷、判分、排行榜/记录
- 视频系统：
  - 管理端：`/api/admin/videos/**`
  - 用户端：`/api/videos/**`
  - 分类端：`/api/video-categories/**`
- 内容运营：`/api/banners/**`、`/api/notices/**`
- AI 猜词：`/api/guess/**`
  - 分类、题目、聊天、记录
- 其他：`/api/stats/**`、`/files/**`
  - 统计接口、文件访问接口
## 配置与运行信息
主要配置文件：

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
默认服务端口：

- `8080`

默认依赖服务：

- MySQL（默认库名：`study_rest_hub`）
- Redis
- MinIO
- DashScope（AI）

### 本地启动（后端）

在 `AI_Study-Rest_Hub/AI_study_rest_hub_server` 目录执行：

```bash
mvn clean package
mvn spring-boot:run
```
### 前端部署（front-end）

将front-end打包放入`nginx-1.20.2/html/` 作为静态站点根目录即可（Nginx/静态文件服务器均可）。  
如果后端地址不是 `http://localhost:8080`，需通过网关/反向代理或重新构建前端来调整 API 目标地址。

## 三、系统联调建议

- 前端静态页面由 Nginx 提供，后端单独运行在 `8080`
- 建议在 Nginx 层配置 `/api` 反向代理到后端，避免跨域及硬编码地址问题
- 登录后确保浏览器 `localStorage.userInfo.token` 存在，后端 JWT 拦截策略才会生效

## 四、注意事项

- `application-dev.yml` 含有真实风格的数据库/Redis/AI/MinIO配置，请务必改为环境变量或私有配置文件管理
- 当前目录中的前端是构建产物，不便直接修改业务代码；若需迭代功能，建议补充前端源码工程目录
- 后端提供了 Knife4j/SpringDoc 配置，可用于接口调试与联调文档化

## 五、可继续完善的方向

- 增加根目录 `docker-compose.yml`（MySQL + Redis + MinIO + backend + nginx）实现一键启动
- 补充数据库初始化脚本与版本迁移（Flyway/Liquibase）
- 为关键接口增加自动化测试与压测脚本

