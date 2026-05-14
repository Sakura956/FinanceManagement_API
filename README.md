# 个人财务管理系统 - 后端

基于 Spring Boot 3 + MyBatis-Plus + Spring Security + JWT 的个人财务管理后端系统，提供收支记账、理财计划管理、统计分析、备忘录与 AI 智能助手等功能。

**前端项目（Vue 3）**：[https://github.com/Sakura956/FinanceManagement_Vue](https://github.com/Sakura956/FinanceManagement_Vue)

## 技术栈

| 技术             | 版本    | 说明                      |
| ---------------- | ------- | ------------------------- |
| Java             | 17      | 运行环境                  |
| Spring Boot      | 3.4.4   | 基础框架                  |
| Spring Security  | 6.x     | 认证与权限控制            |
| MyBatis-Plus     | 3.5.8   | ORM 框架（Spring Boot 3 版） |
| MySQL            | 8.0+    | 关系型数据库              |
| Redis            | 6.0+    | Token 缓存（支持黑名单机制） |
| JWT (jjwt)       | 0.12.6  | 无状态身份认证            |
| Lombok           | -       | 简化代码                  |
| Hutool           | 5.8.34  | 通用工具类库              |
| FastJSON2        | 2.0.53  | JSON 序列化（备选）       |
| DeepSeek (可选)  | R1      | AI 智能助手，通过 SiliconFlow API 调用 |

## 功能模块

### 1. 认证模块 (`/api/v1/auth`)
- 手机号 + 密码注册/登录
- JWT Token 鉴权（Redis 存储，支持强制下线）
- 修改密码、更新个人信息
- BCrypt 密码加密存储

### 2. 收支账单管理 (`/api/v1/user/bills`)
- 记一笔账（收入/支出）
- 账单分页查询（支持多条件筛选：类型、分类、日期范围、金额范围、关键词搜索）
- 账单详情、修改、删除
- 批量删除
- 收支汇总统计（随查询一并返回）

### 3. 账单分类管理
- 用户端：获取可用分类列表 (`/api/v1/user/categories`)
- 管理端：分类 CRUD、启用/禁用、删除保护 (`/api/v1/admin/categories`)

### 4. 理财计划管理 (`/api/v1/user/finance-plans`)
- 创建理财计划（名称、初始金额、当前市值、预期收益率）
- 更新市值/手动估值（自动计算收益与收益率）
- 赎回状态变更
- 投资组合汇总统计

### 5. 统计分析 (`/api/v1/user/statistics`)
- 月度收支总览
- 支出/收入分类占比（饼图数据）
- 近 N 月收支趋势（折线图数据）
- 年度总览（月均支出、最高/最低支出月份）

### 6. 备忘录管理 (`/api/v1/user/memos`)
- 备忘录 CRUD
- 提醒时间设置
- 完成状态切换

### 7. AI 智能助手 (`/api/v1/user/ai`)
- 基于 DeepSeek 大模型的多轮对话
- 对话历史持久化存储
- 支持流式（SSE）与非流式两种响应模式
- 会话列表与消息历史查询

### 8. 管理端 (`/api/v1/admin`) — 需 ADMIN 角色
- 用户列表查询（分页、搜索、状态筛选、日期筛选）
- 用户详情（含账单数、备忘录数、理财计划数）
- 封禁/解封用户（被封禁用户 Token 自动失效）
- 分类管理（CRUD、启用/禁用、默认分类保护）
- 仪表盘统计（总用户数、今日活跃、本周/月新增、总账单数等）

## 项目结构

```
FinanceManagement
├── sql/
│   └── init.sql                       # 数据库初始化脚本（建库 + 建表 + 默认数据）
├── docs/
│   └── API接口详细文档.md               # 完整 API 接口文档
├── src/main/java/com/finance/
│   ├── FinanceManagementApplication.java  # 启动类
│   ├── common/
│   │   ├── config/                     # 配置类（CORS、MyBatis-Plus、Redis）
│   │   ├── exception/                  # 全局异常处理、业务异常
│   │   └── result/                     # 统一响应体（Result、PageResult）
│   ├── security/                       # Spring Security 组件
│   │   ├── SecurityConfig.java         # 安全配置（拦截规则）
│   │   ├── JwtTokenProvider.java       # JWT 生成与解析
│   │   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   │   ├── UserDetailsServiceImpl.java # 用户加载服务
│   │   └── CustomAccessDeniedHandler.java # 权限不足处理器
│   ├── modules/
│   │   ├── auth/                       # 认证模块（注册、登录、修改密码）
│   │   ├── bill/                       # 账单模块（收支记录、分类）
│   │   ├── plan/                       # 理财计划模块
│   │   ├── statistics/                 # 统计分析模块
│   │   ├── memo/                       # 备忘录模块
│   │   ├── ai/                         # AI 智能助手模块
│   │   └── admin/                      # 管理端模块
│   └── util/
│       ├── RedisUtil.java              # Redis 工具类
│       └── SecurityUtil.java           # 安全上下文工具类
└── src/main/resources/
    ├── application.yml                 # 主配置文件（已脱敏，需自行填入敏感信息）
    └── application.yml.example         # 配置文件模板
```

## 快速开始

### 环境要求

- **JDK 17** 或更高
- **MySQL 8.0** 或更高
- **Redis 6.0** 或更高（用于 Token 缓存）
- **Maven 3.6** 或更高

### 1. 创建数据库

执行 `sql/init.sql` 脚本，创建数据库、建表并插入默认分类数据和默认管理员账号。

管理员账号请在应用中用BCryptPasswordEncoder.encode("admin123")生成替换。

```
sql文件里插入的密文仅为格式占位。
-- 正确做法：
-- 方式1：启动项目后，写一个简单测试类生成密文，然后替换
-- 方式2：执行下面 UPDATE 语句替换（需要先生成真实密文）
--
-- 生成密文示例代码：
--   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
--   String encodedPassword = encoder.encode("admin123");
--   System.out.println(encodedPassword);
```

> 默认管理员：`admin` / `admin123`（密码为 BCrypt 加密存储）

### 2. 修改配置文件

打开 `src/main/resources/application.yml`，修改以下配置项：

- **数据库连接**：`spring.datasource.username`、`spring.datasource.password`、`spring.datasource.url`
- **Redis 密码**：`spring.data.redis.password`（如无密码则留空）
- **JWT 密钥**：`jwt.secret`（请改为自己的随机字符串，至少 256 位）
- **AI API Key**（可选）：`ai.api-key`，如不需要 AI 功能可将 `ai.enabled` 设为 `false`

> 具体配置文件见 `src/main/resources/application.yml`或者配置文件具体说明

### 3. 启动项目

```bash
# 开发环境
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/FinanceManagement-0.0.1-SNAPSHOT.jar
```

服务默认运行在 **http://localhost:8080**。

### 4. 验证

```bash
# 测试登录接口
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"admin","password":"admin123"}'
```

## 配置文件说明

项目使用 `application.yml` 作为配置文件，各配置项说明如下：

### 数据源配置 `spring.datasource`

| 参数             | 说明                              |
| ---------------- | --------------------------------- |
| `url`            | 数据库连接地址，修改库名或主机地址 |
| `username`       | 数据库用户名                       |
| `password`       | 数据库密码                         |
| `hikari.*`       | HikariCP 连接池参数，一般使用默认值 |

### Redis 配置 `spring.data.redis`

| 参数       | 说明                        |
| ---------- | --------------------------- |
| `host`     | Redis 服务地址               |
| `port`     | Redis 端口                   |
| `password` | Redis 密码（无密码则留空）    |
| `database` | Redis 数据库编号（默认 0）    |

### JWT 配置 `jwt`

| 参数         | 说明                                                   |
| ------------ | ------------------------------------------------------ |
| `secret`     | JWT 签名密钥，**生产环境务必修改**为随机字符串（≥256位） |
| `expiration` | Token 有效期（毫秒），默认 604800000（7天）             |

### AI 配置 `ai`

| 参数         | 说明                                                     |
| ------------ | -------------------------------------------------------- |
| `enabled`    | 是否启用 AI 功能（`true` / `false`）                      |
| `provider`   | AI 提供商，当前为 `deepseek`                              |
| `model`      | 模型名称                                                  |
| `api-key`    | API 密钥，需到 [SiliconFlow](https://siliconflow.cn) 注册获取 |
| `base-url`   | API 接口地址                                              |
| `timeout`    | 请求超时时间（毫秒）                                       |
| `max-tokens` | 每次回复最大 Token 数                                      |

### MyBatis-Plus 配置 `mybatis-plus`

| 参数                                       | 说明                         |
| ------------------------------------------ | ---------------------------- |
| `configuration.map-underscore-to-camel-case` | 下划线转驼峰，默认开启       |
| `configuration.log-impl`                   | SQL 日志输出，开发时可保留    |
| `global-config.db-config.logic-delete-field` | 逻辑删除字段名               |

### 日志级别 `logging.level`

- `com.finance: debug` — 项目自身日志
- `org.springframework.security: info` — Spring Security 日志

## API 文档

完整 API 接口文档见：[docs/API接口详细文档.md](docs/API接口详细文档.md)

**Base URL**: `http://localhost:8080/api/v1`

除登录、注册外，所有接口需携带 JWT Token：

```
Authorization: Bearer <token>
```

### 接口概览

| 模块         | 接口前缀                    | 说明         |
| ------------ | --------------------------- | ------------ |
| 认证         | `/auth/*`                   | 注册、登录等  |
| 用户账单     | `/user/bills/*`             | 收支记录 CRUD |
| 账单分类     | `/user/categories`          | 可用分类列表  |
| 理财计划     | `/user/finance-plans/*`     | 投资计划管理  |
| 统计分析     | `/user/statistics/*`        | 图表数据     |
| 备忘录       | `/user/memos/*`             | 备忘管理     |
| AI 助手      | `/user/ai/*`                | 智能对话     |
| 管理端       | `/admin/*`                  | 管理员功能    |

## 前端项目

本系统的前端项目使用 **Vue 3** 开发，代码托管在 Gitee：

**[https://github.com/Sakura956/FinanceManagement_Vue](https://github.com/Sakura956/FinanceManagement_Vue)**

请将前后端项目分别克隆后配合运行，前端默认代理后端地址为 `http://localhost:8080`。

