# Architecture Decision Record

> 本文件是项目的架构状态文件（Skill v3.0 Decision Record 规范）。后续一切模块开发引用同一份；
> 新事实导致决策失效时须 Re-open：说明新事实 → 重新决策 → 更新本文件并记录变更原因。

Project Mode: REFACTOR（2026-08-30 第二轮，模式为 REFACTOR；首轮 2026-08-29 为初始重构）
Repository: single-repo（frontend/ + backend/）
Frontend: 单前端 Mode A —— React 18 + TypeScript + Vite 6 + antd-mobile + Zustand + Axios
Backend: 单体 Spring Boot 3.2.5 / Java 21，分层 controller → service → repository(MyBatis-Plus) → entity
Database: MySQL 8 + Flyway（V1 结构 / V2 初始数据 / V3 补齐公共字段；存量库 baseline-version=2 接入）
Auth: JWT（security/ 包：JwtUtil + JwtAuthenticationFilter + UserContext；RBAC：/api/admin/** 需 ADMIN 角色）
CI/CD: GitHub Actions（.github/workflows/ci.yml，test + build，无自动部署）
Observability: Spring Actuator /actuator/health（仅暴露 health/info）+ Nginx /health 代理
Advanced: Redis/MQ/ES/K8s 暂不引入（无真实瓶颈证据）；WebSocket 为业务需要（站内聊天）已内置
Deployment: Docker Compose 本地编排（Nginx 唯一入口 :80，backend/mysql 绑 127.0.0.1）；生产部署待定（无服务器/域名）
Constraints: 文件上传 ≤10MB；图片本地存储 uploads/ 卷；站内聊天 WebSocket /ws/chat

## 本轮（REFACTOR-2）增量决策

| # | 决策 | 理由 | 状态 |
|---|------|------|------|
| 1 | 新建 `frontend/src/constants/`，收敛散落在 6+ 页面的状态魔法值 | 同一映射在 Admin/MyPublish/Publish/Profile 重复出现，改动需多处同步（DRY） | CONFIRMED |
| 2 | 新增 `docs/api.md` 离线 API 摘要 | Skill docs 清单缺失项；Knife4j 仅 dev 开启，生产无文档可用 | CONFIRMED |
| 3 | 新增 `backend/src/main/resources/logback-spring.xml` | 统一日志格式与滚动策略（Skill 后端结构标配） | CONFIRMED |
| 4 | 新增项目级 `CHANGELOG.md` 与 `LICENSE`(MIT) | Skill Mode A 根目录标配；README 已声明 MIT 但无实体文件 | CONFIRMED |
| 5 | **不抽** `frontend/src/layouts/` | BottomTab 组件已承担唯一全局布局职责，无第二种布局需求，抽象无价值（REFACTOR 禁止按模板强行统一） | CONFIRMED |
| 6 | **不建** `frontend/src/assets/` | 当前无参与构建的静态资源（public/ 已覆盖 favicon），空目录反成卫生问题 | CONFIRMED |
| 7 | Husky / ESLint 继续暂缓 | 既定 Next Steps（v2 轮决策），本轮不扩范围 | CONFIRMED |
| 8 | 删除工具遗留物 `reasonix.toml`、`.reasonix/`、`.claude/`、`frontend/.file-versions/` | 用户 2026-08-30 显式确认'孤儿文件全删了'；.gitignore 已加防再生条目 | CONFIRMED |

Assumptions:
- 无生产服务器/域名信息，本轮不涉及真实部署（DEPLOY 模式另启）
- 本轮不引入新依赖（不加 MapStruct/Testcontainers 等），仅结构与文档补齐
- WebSocket 验证依赖本地 compose 栈（当前 healthy），结果仅代表 dev 环境

Status: CONFIRMED
