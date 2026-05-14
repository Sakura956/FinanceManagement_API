## API接口详细文档

### 1 基础约定

**Base URL**: `http://localhost:8080/api/v1`

**通用响应格式**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**业务状态码说明**:

| code | 含义                       |
| ---- | -------------------------- |
| 200  | 请求成功                   |
| 400  | 参数错误                   |
| 401  | 未认证 / Token过期         |
| 403  | 无权限                     |
| 404  | 资源不存在                 |
| 409  | 数据冲突（如手机号已注册） |
| 429  | 请求过于频繁               |
| 500  | 服务器内部错误             |
| 503  | AI服务暂不可用             |

**鉴权方式**:
除登录、注册接口外，所有接口需在请求头携带：

text

```
Authorization: Bearer {token}
```

**分页请求参数**（Query）:

| 参数   | 类型   | 必填 | 默认值     | 说明                 |
| ------ | ------ | ---- | ---------- | -------------------- |
| page   | int    | 否   | 1          | 当前页码，最小为1    |
| size   | int    | 否   | 10         | 每页条数，范围1-100  |
| sortBy | String | 否   | createTime | 排序字段             |
| order  | String | 否   | desc       | 排序方式：asc / desc |

**分页响应格式**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10,
    "records": []
  }
}
```

------

### 2 公共接口 - 认证模块

#### 2.1 用户注册（手机号）

**接口描述**：使用手机号注册新用户账号。

- **URL**: `/auth/register`
- **Method**: `POST`
- **是否认证**: 否

**请求参数**（Body，JSON）:

| 参数     | 类型   | 必填 | 说明                     |
| -------- | ------ | ---- | ------------------------ |
| phone    | String | 是   | 手机号，11位数字         |
| password | String | 是   | 密码，6-20位             |
| nickname | String | 否   | 昵称，不传默认使用手机号 |

**请求示例**:

json

```
{
  "phone": "13800138000",
  "password": "abc123456",
  "nickname": "张三"
}
```

**校验规则**:

- 手机号：正则 `^1[3-9]\d{9}$`，必须未被注册
- 密码：长度6-20位，至少包含字母和数字

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**冲突响应（409）**:

json

```
{
  "code": 409,
  "message": "该手机号已注册",
  "data": null
}
```

**参数错误响应（400）**:

json

```
{
  "code": 400,
  "message": "手机号格式不正确",
  "data": null
}
```

------

#### 2.2 用户登录

**接口描述**：手机号 + 密码登录，管理员与用户共用此接口。

- **URL**: `/auth/login`
- **Method**: `POST`
- **是否认证**: 否

**请求参数**（Body，JSON）:

| 参数     | 类型   | 必填 | 说明   |
| -------- | ------ | ---- | ------ |
| phone    | String | 是   | 手机号 |
| password | String | 是   | 密码   |

**请求示例**:

json

```
{
  "phone": "13800138000",
  "password": "abc123456"
}
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY1MzU2ODAwLCJleHAiOjE3NjU5NjE2MDB9.signature",
    "userInfo": {
      "id": 1,
      "phone": "13800138000",
      "nickname": "张三",
      "role": "USER",
      "avatarUrl": null
    }
  }
}
```

**失败响应（401）**:

json

```
{
  "code": 401,
  "message": "手机号或密码错误",
  "data": null
}
```

**封禁响应（403）**:

json

```
{
  "code": 403,
  "message": "账号已被封禁，请联系管理员",
  "data": null
}
```

**Redis策略**: 登录成功后将Token存入Redis

text

```
Key:   token:user:{userId}
Value: {token值}
TTL:   7天
```

**JWT Payload结构**:

json

```
{
  "userId": 1,
  "role": "USER",
  "iat": 1765356800,
  "exp": 1765961600
}
```

**后续请求鉴权流程**:

1. 从请求头 `Authorization` 获取 Token
2. 解析 JWT 获取 `userId` 和 `role`
3. 从 Redis 中根据 `token:user:{userId}` 获取存储的 Token
4. 比对两个 Token 是否一致（不一致说明已过期或被踢下线）
5. 校验通过，将用户信息存入 `SecurityContextHolder`，放行请求

------

#### 2.3 修改密码

**接口描述**：已登录用户修改自己的密码。

- **URL**: `/auth/change-password`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数        | 类型   | 必填 | 说明                               |
| ----------- | ------ | ---- | ---------------------------------- |
| oldPassword | String | 是   | 旧密码                             |
| newPassword | String | 是   | 新密码，6-20位，至少包含字母和数字 |

**请求示例**:

json

```
{
  "oldPassword": "abc123456",
  "newPassword": "xyz789012"
}
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "密码修改成功，请重新登录",
  "data": null
}
```

**失败响应（400）**:

json

```
{
  "code": 400,
  "message": "旧密码错误",
  "data": null
}
```

**业务逻辑**:

1. 校验旧密码是否正确
2. 更新密码（BCrypt加密存储）
3. 清除 Redis 中该用户的 Token，强制重新登录

------

#### 2.4 获取当前用户信息

**接口描述**：获取已登录用户的详细信息。

- **URL**: `/auth/me`
- **Method**: `GET`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "张三",
    "role": "USER",
    "avatarUrl": null,
    "status": 0,
    "createTime": "2026-01-15 10:30:00"
  }
}
```

------

#### 2.5 更新个人信息

**接口描述**：已登录用户修改自己的昵称、头像等信息。

- **URL**: `/auth/profile`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数      | 类型   | 必填 | 说明           |
| --------- | ------ | ---- | -------------- |
| nickname  | String | 否   | 新昵称，1-20位 |
| avatarUrl | String | 否   | 头像URL地址    |

**请求示例**:

json

```
{
  "nickname": "张三（改名）",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "个人信息更新成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "张三（改名）",
    "role": "USER",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

------

### 3 管理端接口

> **权限要求**: 所有 `/admin/**` 接口需要 `ADMIN` 角色，普通用户访问返回 403。

#### 3.1 用户列表查询

**接口描述**：管理员分页查询所有注册用户。

- **URL**: `/admin/users`
- **Method**: `GET`
- **是否认证**: 是（需 ADMIN 角色）

**请求参数**（Query）:

| 参数      | 类型   | 必填 | 默认值 | 说明                                 |
| --------- | ------ | ---- | ------ | ------------------------------------ |
| page      | int    | 否   | 1      | 页码                                 |
| size      | int    | 否   | 10     | 每页条数，最大100                    |
| keyword   | String | 否   | -      | 手机号或昵称模糊搜索                 |
| status    | int    | 否   | -      | 状态筛选：0-正常，1-封禁。不传查全部 |
| startDate | String | 否   | -      | 注册开始日期，格式 yyyy-MM-dd        |
| endDate   | String | 否   | -      | 注册结束日期，格式 yyyy-MM-dd        |

**请求示例**:

text

```
GET /admin/users?page=1&size=10&keyword=138&status=0
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "page": 1,
    "size": 10,
    "pages": 5,
    "records": [
      {
        "id": 1,
        "phone": "13800138000",
        "nickname": "张三",
        "role": "USER",
        "status": 0,
        "avatarUrl": null,
        "createTime": "2026-01-15 10:30:00"
      },
      {
        "id": 2,
        "phone": "13800138001",
        "nickname": "李四",
        "role": "USER",
        "status": 0,
        "avatarUrl": null,
        "createTime": "2026-02-20 14:20:00"
      }
    ]
  }
}
```

------

#### 3.2 用户详情

**接口描述**：管理员查看指定用户的详细信息及统计数据。

- **URL**: `/admin/users/{userId}`
- **Method**: `GET`
- **是否认证**: 是（需 ADMIN 角色）

**路径参数**:

| 参数   | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| userId | Long | 是   | 用户ID |

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "nickname": "张三",
    "role": "USER",
    "status": 0,
    "avatarUrl": null,
    "billCount": 156,
    "memoCount": 8,
    "planCount": 3,
    "createTime": "2026-01-15 10:30:00",
    "updateTime": "2026-05-10 08:15:00"
  }
}
```

**失败响应（404）**:

json

```
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

------

#### 3.3 封禁/解封用户

**接口描述**：管理员对用户进行封禁或解封操作。

- **URL**: `/admin/users/{userId}/status`
- **Method**: `PUT`
- **是否认证**: 是（需 ADMIN 角色）

**路径参数**:

| 参数   | 类型 | 必填 | 说明   |
| ------ | ---- | ---- | ------ |
| userId | Long | 是   | 用户ID |

**请求参数**（Body，JSON）:

| 参数   | 类型 | 必填 | 说明           |
| ------ | ---- | ---- | -------------- |
| status | int  | 是   | 0-解封，1-封禁 |

**请求示例**:

json

```
{
  "status": 1
}
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "用户状态更新成功",
  "data": null
}
```

**业务逻辑**:

1. 更新用户 `status` 字段
2. 若封禁（status=1），清除该用户在 Redis 中的 Token，强制下线
3. 不能封禁自己

**特殊响应（400）**:

json

```
{
  "code": 400,
  "message": "不能封禁自己的账号",
  "data": null
}
```

------

#### 3.4 账单分类列表

**接口描述**：获取账单分类列表。

- **URL**: `/admin/categories`
- **Method**: `GET`
- **是否认证**: 是（需 ADMIN 角色）

**请求参数**（Query）:

| 参数            | 类型    | 必填 | 默认值 | 说明                       |
| --------------- | ------- | ---- | ------ | -------------------------- |
| type            | int     | 否   | -      | 0-支出，1-收入。不传查全部 |
| includeDisabled | boolean | 否   | false  | 是否包含已禁用分类         |

**请求示例**:

text

```
GET /admin/categories?type=0
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "餐饮",
      "type": 0,
      "icon": "food",
      "sortOrder": 1,
      "isDefault": 1,
      "status": 0,
      "billCount": 1280
    },
    {
      "id": 2,
      "name": "交通",
      "type": 0,
      "icon": "car",
      "sortOrder": 2,
      "isDefault": 1,
      "status": 0,
      "billCount": 567
    }
  ]
}
```

------

#### 3.5 新增账单分类

**接口描述**：管理员新增自定义账单分类。

- **URL**: `/admin/categories`
- **Method**: `POST`
- **是否认证**: 是（需 ADMIN 角色）

**请求参数**（Body，JSON）:

| 参数      | 类型   | 必填 | 说明                 |
| --------- | ------ | ---- | -------------------- |
| name      | String | 是   | 分类名称，2-10个字符 |
| type      | int    | 是   | 0-支出，1-收入       |
| icon      | String | 否   | 图标标识             |
| sortOrder | int    | 否   | 排序序号，默认0      |

**请求示例**:

json

```
{
  "name": "旅行",
  "type": 0,
  "icon": "travel",
  "sortOrder": 15
}
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "分类添加成功",
  "data": {
    "id": 15,
    "name": "旅行",
    "type": 0,
    "icon": "travel",
    "sortOrder": 15,
    "isDefault": 0,
    "status": 0
  }
}
```

**冲突响应（409）**:

json

```
{
  "code": 409,
  "message": "该类型下已存在同名分类",
  "data": null
}

```

------

#### 3.6 修改账单分类

- **URL**: `/admin/categories/{id}`
- **Method**: `PUT`
- **是否认证**: 是（需 ADMIN 角色）

**请求参数**: 与新增相同，支持部分更新（传什么改什么）

**请求示例**:

json

```
{
  "name": "旅游出行",
  "icon": "plane"
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "分类更新成功",
  "data": {
    "id": 15,
    "name": "旅游出行",
    "type": 0,
    "icon": "plane",
    "sortOrder": 15,
    "isDefault": 0,
    "status": 0
  }
}

```

------

#### 3.7 删除/禁用账单分类

**接口描述**：删除或禁用分类。系统默认分类（isDefault=1）不可删除。

- **URL**: `/admin/categories/{id}`
- **Method**: `DELETE`
- **是否认证**: 是（需 ADMIN 角色）

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "分类删除成功",
  "data": null
}

```

**失败响应 - 系统默认分类（400）**:

json

```
{
  "code": 400,
  "message": "系统默认分类不可删除",
  "data": null
}

```

**失败响应 - 有关联账单（409）**:

json

```
{
  "code": 409,
  "message": "该分类下存在 1280 条账单记录，无法删除。建议改为禁用",
  "data": {
    "billCount": 1280
  }
}

```

**业务逻辑**:

1. 若分类下无账单：直接物理删除
2. 若分类下有账单但非默认：返回409，建议前端引导管理员使用“禁用”功能

------

#### 3.8 启用/禁用分类

**接口描述**：启用或禁用某个分类，禁用的分类用户无法选择但已有账单不受影响。

- **URL**: `/admin/categories/{id}/status`
- **Method**: `PUT`
- **是否认证**: 是（需 ADMIN 角色）

**请求参数**（Body，JSON）:

| 参数   | 类型 | 必填 | 说明           |
| ------ | ---- | ---- | -------------- |
| status | int  | 是   | 0-启用，1-禁用 |

**请求示例**:

json

```
{
  "status": 1
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "分类状态更新成功",
  "data": null
}

```

------

#### 3.9 管理端仪表盘

**接口描述**：获取管理端首页统计数据。

- **URL**: `/admin/dashboard`
- **Method**: `GET`
- **是否认证**: 是（需 ADMIN 角色）

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalUsers": 120,
    "activeUsersToday": 45,
    "newUsersThisWeek": 12,
    "newUsersThisMonth": 38,
    "totalBills": 3580,
    "billsToday": 89,
    "totalPlans": 156,
    "totalMemos": 420
  }
}

```

------

### 4 用户端接口 - 收支账单管理

> **权限要求**: 所有用户端接口需要 `USER` 角色，且数据严格按 `userId` 隔离。

#### 4.1 记一笔账

**接口描述**：新增一条收入或支出记录。

- **URL**: `/user/bills`
- **Method**: `POST`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数        | 类型   | 必填 | 说明                               |
| ----------- | ------ | ---- | ---------------------------------- |
| type        | int    | 是   | 0-支出，1-收入                     |
| amount      | double | 是   | 金额，必须 > 0，最多2位小数        |
| categoryId  | long   | 是   | 分类ID（需与type匹配）             |
| description | String | 否   | 备注描述，最多500字                |
| recordTime  | String | 是   | 记录时间，格式 yyyy-MM-dd HH:mm:ss |

**请求示例**:

json

```
{
  "type": 0,
  "amount": 48.50,
  "categoryId": 1,
  "description": "午餐 - 肯德基",
  "recordTime": "2026-05-10 12:30:00"
}

```

**校验规则**:

- `amount` > 0 且最多2位小数
- `categoryId` 对应的分类存在、状态为启用，且分类的 `type` 与参数 `type` 一致
- `recordTime` 不能是未来时间

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "记账成功",
  "data": {
    "id": 1001,
    "type": 0,
    "amount": 48.50,
    "categoryId": 1,
    "categoryName": "餐饮",
    "categoryIcon": "food",
    "description": "午餐 - 肯德基",
    "recordTime": "2026-05-10 12:30:00",
    "createTime": "2026-05-10 12:31:00"
  }
}

```

**失败响应示例 - 分类不匹配（400）**:

json

```
{
  "code": 400,
  "message": "选择的分类与账单类型不匹配",
  "data": null
}

```

------

#### 4.2 账单分页查询

**接口描述**：查询当前用户的账单记录，支持多条件筛选。

- **URL**: `/user/bills`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数       | 类型   | 必填 | 默认值     | 说明                          |
| ---------- | ------ | ---- | ---------- | ----------------------------- |
| page       | int    | 否   | 1          | 页码                          |
| size       | int    | 否   | 10         | 每页条数，最大100             |
| type       | int    | 否   | -          | 0-支出，1-收入。不传查全部    |
| categoryId | long   | 否   | -          | 分类ID筛选                    |
| startDate  | String | 否   | -          | 开始日期，格式 yyyy-MM-dd     |
| endDate    | String | 否   | -          | 结束日期，格式 yyyy-MM-dd     |
| keyword    | String | 否   | -          | 备注描述模糊搜索              |
| minAmount  | double | 否   | -          | 最小金额                      |
| maxAmount  | double | 否   | -          | 最大金额                      |
| sortBy     | String | 否   | recordTime | 排序字段：amount / recordTime |
| order      | String | 否   | desc       | asc / desc                    |

**请求示例**:

text

```
GET /user/bills?page=1&size=10&type=0&startDate=2026-05-01&endDate=2026-05-10&keyword=午餐&sortBy=amount&order=desc

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 25,
    "page": 1,
    "size": 10,
    "pages": 3,
    "summary": {
      "totalIncome": 15000.00,
      "totalExpense": 3245.50
    },
    "records": [
      {
        "id": 1001,
        "type": 0,
        "amount": 48.50,
        "categoryId": 1,
        "categoryName": "餐饮",
        "categoryIcon": "food",
        "description": "午餐 - 肯德基",
        "recordTime": "2026-05-10 12:30:00"
      }
    ]
  }
}

```

> **注意**: `summary` 数据为当前筛选条件下的汇总统计，不受分页影响。

------

#### 4.3 账单详情

**接口描述**：查询单条账单的详细信息。

- **URL**: `/user/bills/{id}`
- **Method**: `GET`
- **是否认证**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明   |
| ---- | ---- | ---- | ------ |
| id   | Long | 是   | 账单ID |

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "type": 0,
    "amount": 48.50,
    "categoryId": 1,
    "categoryName": "餐饮",
    "categoryIcon": "food",
    "description": "午餐 - 肯德基",
    "recordTime": "2026-05-10 12:30:00",
    "createTime": "2026-05-10 12:31:00",
    "updateTime": "2026-05-10 12:31:00"
  }
}

```

**失败响应 - 非本用户或不存在（404）**:

json

```
{
  "code": 404,
  "message": "账单不存在",
  "data": null
}

```

------

#### 4.4 修改账单

**接口描述**：修改账单信息，支持部分更新。

- **URL**: `/user/bills/{id}`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**: 所有字段选填，传什么改什么。

| 参数        | 类型   | 必填 | 说明       |
| ----------- | ------ | ---- | ---------- |
| amount      | double | 否   | 新金额     |
| categoryId  | long   | 否   | 新分类ID   |
| description | String | 否   | 新备注     |
| recordTime  | String | 否   | 新记录时间 |

**请求示例**:

json

```
{
  "amount": 52.00,
  "categoryId": 1,
  "description": "午餐 - 麦当劳（修改过）"
}

```

**校验规则**:

- 修改 `amount` 时，必须 > 0
- 修改 `categoryId` 时，分类存在、启用，且 `type` 与账单 `type` 一致

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "账单修改成功",
  "data": {
    "id": 1001,
    "type": 0,
    "amount": 52.00,
    "categoryId": 1,
    "categoryName": "餐饮",
    "categoryIcon": "food",
    "description": "午餐 - 麦当劳（修改过）",
    "recordTime": "2026-05-10 12:30:00",
    "updateTime": "2026-05-10 14:00:00"
  }
}

```

------

#### 4.5 删除账单

**接口描述**：删除一条账单记录。

- **URL**: `/user/bills/{id}`
- **Method**: `DELETE`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "账单删除成功",
  "data": null
}

```

------

#### 4.6 批量删除账单

**接口描述**：批量删除多条账单。

- **URL**: `/user/bills/batch`
- **Method**: `DELETE`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数 | 类型   | 必填 | 说明               |
| ---- | ------ | ---- | ------------------ |
| ids  | Long[] | 是   | 要删除的账单ID数组 |

**请求示例**:

json

```
{
  "ids": [1001, 1002, 1003]
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "删除成功，共处理 3 条",
  "data": {
    "deletedCount": 3
  }
}

```

#### 4.7用户端账单分类
**接口描述**：用户端获取账单的分类。

- **URL**: `/user/categories?type=0`
- **Method**: `GET`
- **是否认证**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明                                       |
| ---- | ------- | ---- | -------------------------------------- |
| type | Integer | 否   | 分类类型：0 支出 / 1 收入，不传则返回全部 |

**成功响应（200）**:

```
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "工资",
      "type": 1,
      "icon": "salary"
    },
    {
      "id": 3,
      "name": "餐饮",
      "type": 0,
      "icon": "food"
    },
    {
      "id": 4,
      "name": "交通",
      "type": 0,
      "icon": "transport"
    }
  ]
}
```

------

### 5 用户端接口 - 理财计划管理

#### 5.1 获取理财计划列表

- **URL**: `/user/finance-plans`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数   | 类型 | 必填 | 默认值 | 说明                           |
| ------ | ---- | ---- | ------ | ------------------------------ |
| status | int  | 否   | -      | 0-持有中，1-已赎回。不传查全部 |
| page   | int  | 否   | 1      | 页码                           |
| size   | int  | 否   | 10     | 每页条数                       |

**请求示例**:

text

```
GET /user/finance-plans?status=0&page=1&size=10

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "pages": 1,
    "summary": {
      "totalInvested": 50000.00,
      "totalCurrentValue": 58600.50,
      "totalProfit": 8600.50,
      "overallProfitRate": 17.20
    },
    "records": [
      {
        "id": 1,
        "name": "沪深300指数基金定投",
        "initialAmount": 10000.00,
        "currentValue": 12500.50,
        "profitAmount": 2500.50,
        "profitRate": 25.01,
        "expectedRoi": 8.00,
        "startDate": "2025-06-01",
        "endDate": null,
        "status": 0,
        "remark": "每月定投1000元",
        "createTime": "2025-06-01 00:00:00",
        "updateTime": "2026-05-10 09:00:00"
      },
      {
        "id": 2,
        "name": "某理财产品",
        "initialAmount": 20000.00,
        "currentValue": 21000.00,
        "profitAmount": 1000.00,
        "profitRate": 5.00,
        "expectedRoi": 4.50,
        "startDate": "2026-01-15",
        "endDate": "2026-07-15",
        "status": 0,
        "remark": "半年期理财",
        "createTime": "2026-01-15 10:00:00",
        "updateTime": "2026-05-10 09:00:00"
      }
    ]
  }
}

```

> **注意**: `summary` 为所有理财计划的汇总统计（不受分页影响）。`profitAmount` 和 `profitRate` 为后端计算得出，不在数据库中存储。

------

#### 5.2 理财计划详情

- **URL**: `/user/finance-plans/{id}`
- **Method**: `GET`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "沪深300指数基金定投",
    "initialAmount": 10000.00,
    "currentValue": 12500.50,
    "profitAmount": 2500.50,
    "profitRate": 25.01,
    "expectedRoi": 8.00,
    "startDate": "2025-06-01",
    "endDate": null,
    "status": 0,
    "remark": "每月定投1000元",
    "createTime": "2025-06-01 00:00:00",
    "updateTime": "2026-05-10 09:00:00"
  }
}

```

------

#### 5.3 创建理财计划

- **URL**: `/user/finance-plans`
- **Method**: `POST`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数          | 类型   | 必填 | 说明                      |
| ------------- | ------ | ---- | ------------------------- |
| name          | String | 是   | 计划名称，1-50字          |
| initialAmount | double | 是   | 初始投入金额，> 0         |
| currentValue  | double | 是   | 当前市值，>= 0            |
| expectedRoi   | double | 否   | 预期年化收益率(%)         |
| startDate     | String | 是   | 开始日期，格式 yyyy-MM-dd |
| endDate       | String | 否   | 结束日期，格式 yyyy-MM-dd |
| remark        | String | 否   | 备注，最多500字           |

**请求示例**:

json

```
{
  "name": "沪深300指数基金定投",
  "initialAmount": 10000.00,
  "currentValue": 12500.50,
  "expectedRoi": 8.00,
  "startDate": "2025-06-01",
  "endDate": null,
  "remark": "每月定投1000元"
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "理财计划创建成功",
  "data": {
    "id": 1,
    "name": "沪深300指数基金定投",
    "initialAmount": 10000.00,
    "currentValue": 12500.50,
    "profitAmount": 2500.50,
    "profitRate": 25.01,
    "expectedRoi": 8.00,
    "startDate": "2025-06-01",
    "status": 0
  }
}

```

------

#### 5.4 修改理财计划基本信息

**接口描述**：修改计划的基础信息（名称、预期收益率、备注等），不涉及市值变动。

- **URL**: `/user/finance-plans/{id}`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**: 支持部分更新。

**请求示例**:

json

```
{
  "name": "沪深300指数基金定投（修改名称）",
  "expectedRoi": 10.00,
  "remark": "调整为每月定投2000元"
}
请求可多参数，已经改进
```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "理财计划更新成功",
  "data": { ... }
}

```

------

#### 5.5 更新理财计划市值/收益

**接口描述**：手动更新理财计划的当前市值，系统自动计算收益。

- **URL**: `/user/finance-plans/{id}/valuation`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数         | 类型   | 必填 | 说明           |
| ------------ | ------ | ---- | -------------- |
| currentValue | double | 是   | 最新市值，>= 0 |

**请求示例**:

json

```
{
  "currentValue": 13500.00
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "市值更新成功",
  "data": {
    "id": 1,
    "initialAmount": 10000.00,
    "currentValue": 13500.00,
    "profitAmount": 3500.00,
    "profitRate": 35.00,
    "updateTime": "2026-05-10 16:00:00"
  }
}

```

**业务逻辑**:

1. 更新 `current_value` 字段
2. 返回时计算 `profitAmount = currentValue - initialAmount`
3. 计算 `profitRate = (profitAmount / initialAmount) * 100`

------

#### 5.6 理财计划状态变更（赎回）

**接口描述**：将理财计划标记为“已赎回”状态。

- **URL**: `/user/finance-plans/{id}/status`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数    | 类型   | 必填 | 说明                                    |
| ------- | ------ | ---- | --------------------------------------- |
| status  | int    | 是   | 1-已赎回                                |
| endDate | String | 否   | 赎回日期，格式 yyyy-MM-dd，不传默认当天 |

**请求示例**:

json

```
{
  "status": 1,
  "endDate": "2026-05-10"
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "该理财计划已标记为已赎回",
  "data": null
}

```

------

#### 5.7 删除理财计划

- **URL**: `/user/finance-plans/{id}`
- **Method**: `DELETE`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "理财计划删除成功",
  "data": null
}

```

------

### 6 用户端接口 - 统计分析

#### 6.1 月度收支总览

**接口描述**：获取指定月份的收支总览数据。

- **URL**: `/user/statistics/overview`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数  | 类型   | 必填 | 说明                               |
| ----- | ------ | ---- | ---------------------------------- |
| month | String | 否   | 月份，格式 yyyy-MM。不传默认当前月 |

**请求示例**:

text

```
GET /user/statistics/overview?month=2026-05

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "month": "2026-05",
    "income": 20000.00,
    "expense": 6500.50,
    "balance": 13499.50
  }
}

```

------

#### 6.2 支出分类统计（饼图数据）

**接口描述**：获取指定月份各支出分类的占比数据。

- **URL**: `/user/statistics/category-pie`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数  | 类型   | 必填 | 说明                               |
| ----- | ------ | ---- | ---------------------------------- |
| month | String | 否   | 月份，格式 yyyy-MM。不传默认当前月 |
| type  | int    | 否   | 0-支出，1-收入。不传默认0          |

**请求示例**:

text

```
GET /user/statistics/category-pie?month=2026-05&type=0

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalAmount": 6500.50,
    "items": [
      {
        "categoryId": 1,
        "categoryName": "餐饮",
        "categoryIcon": "food",
        "amount": 2500.00,
        "percent": "38.46%",
        "count": 45
      },
      {
        "categoryId": 3,
        "categoryName": "购物",
        "categoryIcon": "shopping",
        "amount": 1500.00,
        "percent": "23.07%",
        "count": 12
      },
      {
        "categoryId": 2,
        "categoryName": "交通",
        "categoryIcon": "car",
        "amount": 800.50,
        "percent": "12.31%",
        "count": 30
      }
    ]
  }
}

```

> **排序**: 按 `amount` 降序排列。

------

#### 6.3 月度趋势（折线图数据）

**接口描述**：获取近 N 个月的收支趋势数据。

- **URL**: `/user/statistics/trend`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数   | 类型 | 必填 | 默认值 | 说明                           |
| ------ | ---- | ---- | ------ | ------------------------------ |
| months | int  | 否   | 12     | 查询最近几个月的趋势，范围1-24 |

**请求示例**:

text

```
GET /user/statistics/trend?months=6

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "month": "2025-12",
      "income": 12000.00,
      "expense": 5200.00,
      "balance": 6800.00
    },
    {
      "month": "2026-01",
      "income": 15000.00,
      "expense": 4800.00,
      "balance": 10200.00
    },
    {
      "month": "2026-02",
      "income": 13000.00,
      "expense": 7100.00,
      "balance": 5900.00
    },
    {
      "month": "2026-03",
      "income": 16000.00,
      "expense": 5500.00,
      "balance": 10500.00
    },
    {
      "month": "2026-04",
      "income": 14000.00,
      "expense": 6200.00,
      "balance": 7800.00
    },
    {
      "month": "2026-05",
      "income": 20000.00,
      "expense": 6500.50,
      "balance": 13499.50
    }
  ]
}

```

> **排序**: 按月份升序排列。

------

#### 6.4 年度总览

**接口描述**：获取指定年份的年度汇总数据。

- **URL**: `/user/statistics/yearly`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 说明                 |
| ---- | ---- | ---- | -------------------- |
| year | int  | 否   | 年份，不传默认当前年 |

**请求示例**:

text

```
GET /user/statistics/yearly?year=2026

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "year": 2026,
    "totalIncome": 98000.00,
    "totalExpense": 52100.50,
    "balance": 45899.50,
    "monthlyAvgExpense": 10420.10,
    "highestExpenseMonth": "2026-02",
    "highestExpenseAmount": 7100.00,
    "lowestExpenseMonth": "2026-01",
    "lowestExpenseAmount": 4800.00
  }
}

```

月均支出: 总支出 / 有数据的月份数。

------

### 7 用户端接口 - 备忘录管理

#### 7.1 备忘录列表

- **URL**: `/user/memos`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数        | 类型   | 必填 | 默认值 | 说明                           |
| ----------- | ------ | ---- | ------ | ------------------------------ |
| isCompleted | int    | 否   | -      | 0-未完成，1-已完成。不传查全部 |
| page        | int    | 否   | 1      | 页码                           |
| size        | int    | 否   | 10     | 每页条数                       |
| keyword     | String | 否   | -      | 标题模糊搜索                   |

**请求示例**:

text

```
GET /user/memos?isCompleted=0&keyword=还款

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 8,
    "page": 1,
    "size": 10,
    "pages": 1,
    "records": [
      {
        "id": 1,
        "title": "还信用卡",
        "content": "招商银行信用卡还款，最低还款额500元",
        "remindTime": "2026-05-15 09:00:00",
        "isCompleted": 0,
        "createTime": "2026-05-01 10:00:00",
        "updateTime": "2026-05-01 10:00:00"
      },
      {
        "id": 2,
        "title": "交房租",
        "content": "5月份房租，金额3500元",
        "remindTime": "2026-05-30 18:00:00",
        "isCompleted": 0,
        "createTime": "2026-05-03 08:00:00",
        "updateTime": "2026-05-03 08:00:00"
      }
    ]
  }
}

```

------

#### 7.2 备忘录详情

- **URL**: `/user/memos/{id}`
- **Method**: `GET`
- **是否认证**: 是

**成功响应**: 同列表中的单条记录详情。

------

#### 7.3 新增备忘录

- **URL**: `/user/memos`
- **Method**: `POST`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数       | 类型   | 必填 | 说明                               |
| ---------- | ------ | ---- | ---------------------------------- |
| title      | String | 是   | 标题，1-100字                      |
| content    | String | 否   | 内容详情                           |
| remindTime | String | 否   | 提醒时间，格式 yyyy-MM-dd HH:mm:ss |

**请求示例**:

json

```
{
  "title": "还花呗",
  "content": "5月份花呗账单，还款金额2800元",
  "remindTime": "2026-05-20 09:00:00"
}

```

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "备忘录创建成功",
  "data": {
    "id": 10,
    "title": "还花呗",
    "content": "5月份花呗账单，还款金额2800元",
    "remindTime": "2026-05-20 09:00:00",
    "isCompleted": 0,
    "createTime": "2026-05-10 15:00:00"
  }
}

```

------

#### 7.4 修改备忘录

- **URL**: `/user/memos/{id}`
- **Method**: `PUT`
- **是否认证**: 是

**请求参数**: 支持部分更新。

**请求示例**:

json

```
{
  "title": "还花呗（已修改）",
  "content": "调整还款金额至3000元",
  "remindTime": "2026-05-21 09:00:00"
}

```

------

#### 7.5 完成/取消完成备忘录

- **URL**: `/user/memos/{id}/toggle`
- **Method**: `PUT`
- **是否认证**: 是

**接口描述**: 切换完成状态（已完成 → 未完成，未完成 → 已完成）。

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "状态切换成功",
  "data": {
    "id": 10,
    "isCompleted": 1,
    "updateTime": "2026-05-15 14:00:00"
  }
}

```

------

#### 7.6 删除备忘录

- **URL**: `/user/memos/{id}`
- **Method**: `DELETE`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "备忘录删除成功",
  "data": null
}

```

------

### 8 用户端接口 - AI智能助手

#### 8.1 模块概述

AI智能助手模块旨在为用户提供智能化的财务管理建议和对话服务。

**设计原则**:

- 接口与具体大模型解耦，通过配置切换
- 对话历史持久化存储，支持多轮对话
- 采用流式（SSE）或非流式两种响应模式

------

#### 8.2 发送对话消息

**接口描述**：用户向 AI 发送消息，获取回复。

- **URL**: `/user/ai/chat`
- **Method**: `POST`
- **是否认证**: 是

**请求参数**（Body，JSON）:

| 参数           | 类型    | 必填 | 说明                         |
| -------------- | ------- | ---- | ---------------------------- |
| sessionId      | String  | 否   | 会话ID。不传则创建新会话     |
| message        | String  | 是   | 用户消息内容，1-2000字       |
| includeHistory | boolean | 否   | 是否携带历史上下文，默认true |

**请求示例**（新会话）:

json

```
{
  "message": "我本月餐饮支出2500元，占总支出38%，这个比例是否合理？有什么优化建议？"
}

```

**请求示例**（继续对话）:

json

```
{
  "sessionId": "sess_a1b2c3d4",
  "message": "具体怎么控制餐饮支出？",
  "includeHistory": true
}

```

**非流式响应 - 成功（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "sessionId": "sess_a1b2c3d4",
    "message": "根据您的情况，餐饮支出占总支出的38%确实偏高，一般建议控制在20%-30%之间。以下是一些优化建议：\n\n1. **减少外卖频率**：自己做饭可以节省约40%的餐饮开支。\n2. **制定每周预算**：将1500元分配到4周，每周约375元。\n3. **咖啡奶茶优化**：如果每天一杯，改为隔天一杯，每月可省200-300元。\n\n您可以尝试上述方法，预计月节省800-1000元。如需更详细的预算计划，请告诉我！",
    "tokensUsed": 350,
    "createdAt": "2026-05-10 15:30:00"
  }
}

```

**流式响应（SSE）**:

接口地址相同，请求头增加：

text

```
Accept: text/event-stream

```

响应格式为 SSE（Server-Sent Events）：

text

```
data: {"token":"根据","sessionId":"sess_a1b2c3d4"}

data: {"token":"您"}

data: {"token":"的"}

data: {"token":"情况"}

...

data: {"token":"!","sessionId":"sess_a1b2c3d4","tokensUsed":350,"finished":true}

```

------

#### 8.3 获取会话列表

**接口描述**：获取当前用户的所有 AI 对话会话列表。

- **URL**: `/user/ai/sessions`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 默认值 | 说明     |
| ---- | ---- | ---- | ------ | -------- |
| page | int  | 否   | 1      | 页码     |
| size | int  | 否   | 10     | 每页条数 |

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "records": [
      {
        "sessionId": "sess_a1b2c3d4",
        "title": "餐饮支出优化建议",
        "lastMessage": "具体怎么控制餐饮支出？",
        "messageCount": 6,
        "lastActiveTime": "2026-05-10 15:30:00",
        "createTime": "2026-05-10 15:20:00"
      },
      {
        "sessionId": "sess_x9y8z7",
        "title": "理财规划咨询",
        "lastMessage": "推荐一些低风险的理财方式",
        "messageCount": 4,
        "lastActiveTime": "2026-05-09 10:00:00",
        "createTime": "2026-05-09 09:50:00"
      }
    ]
  }
}

```

> **注意**: `title` 取该会话第一条用户消息的前30个字符；`lastMessage` 取最近一条用户消息内容。

------

#### 8.4 获取会话消息历史

**接口描述**：获取指定会话的所有消息记录。

- **URL**: `/user/ai/sessions/{sessionId}/messages`
- **Method**: `GET`
- **是否认证**: 是

**请求参数**（Query）:

| 参数 | 类型 | 必填 | 默认值 | 说明     |
| ---- | ---- | ---- | ------ | -------- |
| page | int  | 否   | 1      | 页码     |
| size | int  | 否   | 20     | 每页条数 |

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 6,
    "page": 1,
    "size": 20,
    "records": [
      {
        "id": 101,
        "role": "user",
        "content": "我本月餐饮支出2500元，占总支出38%，这个比例是否合理？有什么优化建议？",
        "tokensUsed": 0,
        "createTime": "2026-05-10 15:20:00"
      },
      {
        "id": 102,
        "role": "assistant",
        "content": "根据您的情况，餐饮支出占总支出的38%确实偏高...",
        "tokensUsed": 200,
        "createTime": "2026-05-10 15:20:05"
      },
      {
        "id": 103,
        "role": "user",
        "content": "具体怎么控制餐饮支出？",
        "tokensUsed": 0,
        "createTime": "2026-05-10 15:29:00"
      },
      {
        "id": 104,
        "role": "assistant",
        "content": "以下是一些具体的控制方法...",
        "tokensUsed": 150,
        "createTime": "2026-05-10 15:30:00"
      }
    ]
  }
}

```

> **排序**: 按 `createTime` 升序排列。

------

#### 8.5 删除会话

**接口描述**：删除指定会话及其所有消息记录。

- **URL**: `/user/ai/sessions/{sessionId}`
- **Method**: `DELETE`
- **是否认证**: 是

**成功响应（200）**:

json

```
{
  "code": 200,
  "message": "会话删除成功",
  "data": null
}

```

------

