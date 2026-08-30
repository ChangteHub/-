# API 接口摘要（离线版）

> 在线交互文档（Knife4j）仅 dev 环境开启：`http://localhost:8080/doc.html`；生产已关闭（防接口泄露）。
> 本文件是离线摘要，与代码同步维护；机械校验跑 `check-api-contract.py`。

## 通用约定

- **Base Path**：`/api`；Nginx 同源反代（浏览器直接请求 `/api/**`，无需跨域）
- **认证**：登录后携带 `Authorization: Bearer <token>`；token 有效期 24h
- **统一响应**：`{ "code": 200, "message": "success", "data": ... }`；错误码语义：400 参数错误 / 401 未认证 / 403 无权限 / 404 不存在 / 500 服务器异常
- **分页响应**：`{ total, pageNum, pageSize, pages, list }`，`pageNum` 从 1 开始
- **实时通信**：WebSocket `/ws/chat`（连接时以 query 参数携带 token 鉴权）

## 认证 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/auth/register` | 注册（username/password/nickname/studentId） | 公开 |
| POST | `/auth/login` | 登录 → `{token, user}` | 公开 |
| GET | `/auth/me` | 当前用户信息 | 需要 |

## 商品 `/api/product` `/api/category` `/api/search`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/product/list` | 商品列表（categoryId/keyword/sort/分页） | 公开 |
| GET | `/product/{id}` | 商品详情（自增浏览历史） | 公开 |
| POST | `/product` | 发布商品 | 需要 |
| PUT | `/product/{id}` | 编辑商品（仅卖家） | 需要 |
| DELETE | `/product/{id}` | 删除商品（逻辑删除，仅卖家） | 需要 |
| PUT | `/product/{id}/status` | 上下架/标记售出（仅卖家） | 需要 |
| GET | `/category/list` | 分类列表 | 公开 |
| GET | `/search/hot` `/search/history` | 热搜 / 个人搜索历史 | 历史:需要 |
| DELETE | `/search/history` `/search/history/{keyword}` | 清空/删除单条搜索历史 | 需要 |

## 用户 `/api/user` `/api/favorite` `/api/history` `/api/verification` `/api/upload`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| PUT | `/user/profile` | 更新资料（nickname/avatar/phone/bio） | 需要 |
| GET | `/user/products` | 我的发布（status 过滤分页） | 需要 |
| GET | `/user/favorites` | 我的收藏（分页） | 需要 |
| POST/DELETE | `/favorite/{productId}` | 收藏/取消收藏（幂等） | 需要 |
| GET | `/favorite/check/{productId}` | 是否已收藏 → boolean | 需要 |
| GET/POST/DELETE | `/history` 系列 | 浏览历史（列表/上报/清空/删单条） | 需要 |
| POST | `/verification` | 提交实名认证（realName/studentId/college/enrollYear/studentCardUrl） | 需要 |
| GET | `/verification/status` | 我的认证状态 | 需要 |
| POST | `/upload/image` | 上传图片（multipart，≤10MB）→ `{url}` | 需要 |

## 聊天 `/api/chat` + WebSocket

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/chat/sessions` | 会话列表（含未读数） | 需要 |
| POST | `/chat/session` | 创建/获取会话（productId+targetUserId，幂等） | 需要 |
| GET | `/chat/session/{id}` | 会话详情 | 需要（限买卖双方） |
| GET | `/chat/messages/{sessionId}` | 消息分页 | 需要 |
| PUT | `/chat/messages/{sessionId}/read` | 标记会话已读 | 需要 |

WebSocket：连接 `/ws/chat?token=...`；入站消息 `{type, sessionId, content}`；服务端推送消息实体 JSON。

## 管理员 `/api/admin`（全部需要 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/dashboard` | 统计看板 |
| GET | `/admin/users` / `/admin/users/{id}` | 用户列表（keyword/status/分页）/ 详情 |
| PUT | `/admin/users/{id}/status` | 禁用/启用（不可操作管理员） |
| GET | `/admin/products` | 商品列表（keyword/status/categoryId/分页） |
| PUT | `/admin/products/{id}/status` | 修改商品状态 |
| DELETE | `/admin/products/{id}` | 删除商品 |
| GET | `/admin/verifications` | 认证审核列表（status/分页） |
| PUT | `/admin/verifications/{id}/review` | 审核通过/拒绝（拒绝必填 rejectReason） |
| GET/POST/PUT/DELETE | `/admin/categories` 系列 | 分类增删改查 + `/status` 启停（有商品时禁删） |

## 其他公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/banners` | 首页轮播图 |
| GET | `/help` | 帮助中心（category/keyword 过滤） |
| GET | `/actuator/health` | 健康检查（Nginx `/health` 代理同源可用） |
