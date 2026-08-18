# 西南科技大学校园二手交易平台

一个面向西南科技大学在校学生的C2C二手物品交易平台。卖家发布商品，买家通过站内聊天联系，线下在校内交易。

## 技术栈

### 后端
- JDK 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- Spring Security + JWT
- MySQL 5.7
- WebSocket
- Knife4j (Swagger)

### 前端
- React 18
- TypeScript
- Vite
- Ant Design Mobile
- Zustand (状态管理)
- Axios

## 项目结构

```
project1/
├── back/                          # 后端项目
│   ├── src/main/
│   │   ├── java/com/xust/secondhand/
│   │   │   ├── SecondhandApplication.java  # 启动类
│   │   │   ├── config/                     # 配置类
│   │   │   ├── controller/                 # 控制器
│   │   │   ├── service/                    # 服务层
│   │   │   ├── mapper/                     # MyBatis Mapper
│   │   │   ├── entity/                     # 实体类
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   ├── vo/                         # 视图对象
│   │   │   ├── common/                     # 公共类
│   │   │   ├── utils/                      # 工具类
│   │   │   └── websocket/                  # WebSocket处理
│   │   └── resources/
│   │       ├── application.yml             # 配置文件
│   │       ├── schema.sql                  # 建表脚本
│   │       └── data.sql                    # 初始化数据
│   └── pom.xml
│
├── frontend/                      # 前端项目
│   ├── src/
│   │   ├── components/            # 公共组件
│   │   ├── pages/                 # 页面组件
│   │   ├── services/              # API服务
│   │   ├── store/                 # 状态管理
│   │   ├── types/                 # 类型定义
│   │   ├── hooks/                 # 自定义Hook
│   │   └── router/                # 路由配置
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 5.7+
- Node.js 16+

### 1. 创建数据库

```sql
CREATE DATABASE xust_secondhand DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置后端

修改 `back/src/main/resources/application.yml` 或设置环境变量：

```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

**必填环境变量**：

| 变量 | 说明 |
|------|------|
| `JWT_SECRET` | JWT 签名密钥（无默认值，**缺失则启动失败**；生产环境务必使用强随机值） |
| `DB_PASSWORD` | 数据库密码（可选，默认 115417，生产必须覆盖） |
| `ADMIN_PASSWORD` | 管理员初始密码（可选，不设置则随机生成一次性密码并打印在启动日志） |

### 3. 建表与初始化

```bash
# 新库：执行 schema.sql 建表（含 role 字段）
mysql -uroot -p xust_secondhand < back/src/main/resources/schema.sql
# 再执行 data.sql 写入分类与测试账号
mysql -uroot -p xust_secondhand < back/src/main/resources/data.sql

# 旧库升级：执行 migration.sql 为 user 表补充 role 字段（幂等，可重复执行）
mysql -uroot -p xust_secondhand < back/src/main/resources/migration.sql
```

### 4. 启动后端

```bash
cd back
export JWT_SECRET=your-strong-secret
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`（首次启动若库中无管理员，将自动创建 `admin` 账号，初始密码见 `ADMIN_PASSWORD` 或启动日志）

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:3000`

### 6. 访问

- 前端：http://localhost:3000
- 接口文档：http://localhost:8080/doc.html
- 管理后台：http://localhost:3000/admin（需管理员账号登录）

## 测试账号

> 测试账号由 `back/src/main/resources/data.sql` 初始化。由于 `spring.sql.init.mode=never`，新库需手动执行一次 `data.sql`（或删除已有库后重启），账号才会存在。

| 用户名 | 密码 | 昵称 | 角色 |
|--------|------|------|------|
| test1 | 115417 | 测试用户1 | 普通用户 |
| test2 | 115417 | 测试用户2 | 普通用户 |
| admin | 系统自动创建，密码见 `ADMIN_PASSWORD` 或启动日志 | 系统管理员 | 管理员 |

> ⚠️ `data.sql` 不包含 admin 账号；管理员由后端启动时自动创建（`DataInitializer`），初始密码来自环境变量 `ADMIN_PASSWORD`，未设置则随机生成并仅打印一次到启动日志。

## API接口

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/me | 获取当前用户 |

### 商品接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/product | 发布商品 |
| GET | /api/product/list | 商品列表 |
| GET | /api/product/{id} | 商品详情 |
| PUT | /api/product/{id} | 编辑商品 |
| DELETE | /api/product/{id} | 删除商品 |

### 用户接口
| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | /api/user/profile | 更新资料 |
| GET | /api/user/products | 我的发布 |
| GET | /api/user/favorites | 我的收藏 |

### 收藏接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/favorite/{id} | 添加收藏 |
| DELETE | /api/favorite/{id} | 取消收藏 |
| GET | /api/favorite/check/{id} | 检查收藏 |

### 聊天接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/chat/sessions | 会话列表 |
| GET | /api/chat/session/{id} | 会话详情 |
| POST | /api/chat/session | 创建会话 |
| GET | /api/chat/messages/{id} | 消息列表 |
| PUT | /api/chat/messages/{id}/read | 标记已读 |

### 其他接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/category/list | 分类列表 |
| GET | /api/banners | 轮播图 |
| GET | /api/help | 帮助列表 |
| POST | /api/upload/image | 上传图片 |
| POST | /api/verification | 提交认证 |
| GET | /api/verification/status | 认证状态 |

### 管理员接口（需 ADMIN 角色，`/admin` 后台对应）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/dashboard | 统计数据 |
| GET | /api/admin/users | 用户列表（keyword/status/分页） |
| GET | /api/admin/users/{id} | 用户详情 |
| PUT | /api/admin/users/{id}/status | 禁用/启用用户（不能操作管理员） |
| GET | /api/admin/products | 商品列表（keyword/status/categoryId/分页） |
| PUT | /api/admin/products/{id}/status | 修改商品状态 |
| DELETE | /api/admin/products/{id} | 删除商品 |
| GET | /api/admin/verifications | 认证列表（status/分页） |
| PUT | /api/admin/verifications/{id}/review | 审核认证（通过/拒绝，拒绝必填原因） |
| GET | /api/admin/categories | 分类列表 |
| POST | /api/admin/categories | 添加分类 |
| PUT | /api/admin/categories/{id} | 修改分类 |
| DELETE | /api/admin/categories/{id} | 删除分类（分类下有商品时拒绝） |
| PUT | /api/admin/categories/{id}/status | 修改分类状态 |

## 数据库表结构

| 表名 | 说明 |
|------|------|
| user | 用户表 |
| category | 分类表 |
| product | 商品表 |
| product_image | 商品图片表 |
| favorite | 收藏表 |
| chat_session | 会话表 |
| chat_message | 消息表 |
| browsing_history | 浏览历史表 |
| verification | 实名认证表 |
| admin_log | 管理员操作日志表 |

## 功能特性

- ✅ 用户注册/登录（JWT认证）
- ✅ 商品发布/编辑/删除
- ✅ 商品分类/搜索/排序
- ✅ 商品收藏
- ✅ 浏览历史
- ✅ 站内聊天（WebSocket）
- ✅ 文件上传
- ✅ 实名认证
- ✅ 深色模式
- ✅ 响应式设计

## 部署说明

### 后端部署

```bash
# 打包
cd back
mvn clean package

# 运行
java -jar target/secondhand-1.0.0.jar
```

### 前端部署

```bash
# 打包
cd frontend
npm run build

# 部署dist目录到Nginx
```

### Nginx配置

```nginx
server {
    listen 80;
    server_name your_domain;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /uploads {
        proxy_pass http://localhost:8080;
    }

    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 开发说明

### 后端开发
- 使用Lombok简化代码
- MyBatis-Plus自动生成SQL
- Spring Security + JWT认证
- WebSocket实时聊天

### 前端开发
- TypeScript类型安全
- Zustand状态管理
- Ant Design Mobile组件库
- Axios HTTP请求

## 许可证

MIT License
