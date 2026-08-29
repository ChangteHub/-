# 系统架构

## 概述

面向西南科技大学在校学生的 C2C 二手物品交易平台。卖家发布商品，买家通过站内聊天联系，线下校内交易。

- 架构模式：**Mode A 简单单体**（单前端 + 单体后端 + MySQL）
- 前端为移动端 H5（含 `/admin` 管理页）

## 技术栈

| Layer | Technology |
|-------|-----------|
| 前端 | React 18 + TypeScript + Vite 6 |
| UI | Ant Design Mobile 5 |
| 状态 | Zustand |
| HTTP | Axios（统一拦截器） |
| 后端 | Java 21 + Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.5 |
| 安全 | Spring Security + JWT（jjwt 0.12） |
| 数据库 | MySQL 8.0 + Flyway |
| 实时通信 | WebSocket（`/ws/chat`） |
| API 文档 | Knife4j（仅 dev profile 开启） |
| 部署 | Docker Compose + Nginx |
| CI | GitHub Actions |

## 请求链路

```
Browser (pages/)
  → React 调 services/（HTTP 层，Axios 拦截器注入 Bearer token）
  → Nginx（静态托管 + /api /uploads /ws 反代）
  → Controller（参数校验 @Valid）
  → Service（业务逻辑 + 事务）
  → Mapper（MyBatis-Plus 数据访问）
  → MySQL（Flyway 管理 schema）
  ← Result<DTO> 统一包装 JSON 返回
```

## 后端分层（backend/）

```
com.xust.secondhand
├── controller/    # REST 控制器（12 个）
├── service/       # 业务接口 + impl/ 实现
├── repository/    # 数据访问层（MyBatis-Plus BaseMapper 接口）
├── entity/        # 数据库实体
├── dto/request/   # 请求 DTO（XxxRequest）
├── dto/response/  # 响应 DTO（XxxResponse）
├── config/        # Security/Web/MyBatis-Plus/WebSocket/Knife4j 配置
├── security/      # JwtUtil / JwtAuthenticationFilter / UserContext（认证层）
├── exception/     # GlobalExceptionHandler（全局异常 → 统一错误 JSON）
├── common/        # Result / PageResult / BusinessException
└── websocket/     # 聊天 WebSocket handler + 鉴权拦截器
```

> 注：后端 `repository/` 内是 MyBatis-Plus BaseMapper 接口，承担数据访问层职责。后端 `service/` 是业务逻辑层，与前端 `services/`（HTTP 通信层）职责不同。

## 前端分层（frontend/src/）

```
src/
├── services/      # HTTP 通信层：request.ts（Axios 实例+拦截器）+ 按业务域拆分的模块
├── stores/        # Zustand 全局状态
├── pages/         # 业务页面（Home / ProductDetail / Chat / Admin 等 16 个）
├── components/    # 可复用组件
├── router/        # 路由配置 + 守卫
├── hooks/ types/ utils/ constants/ assets/
```

## 边界约定

- **事务**：只加在 Service 层；读多写少查询 `readOnly=true`；业务原子性优先，不机械加注解
- **鉴权**：认证（JWT 解析）在 `JwtAuthenticationFilter`；授权（角色判断）在 SecurityConfig 路径规则 + 业务处按 `UserContext` 校验
- **失败路径**：业务失败抛 `BusinessException` → `GlobalExceptionHandler` 转标准错误 JSON；前端拦截器统一拆包 `{code,message,data}`，401 清理凭证、403 明确提示

## 明确不引入

Redis / MQ / Elasticsearch / Kubernetes / 微服务——当前无真实瓶颈证据，遵循 Minimal Stack First。
