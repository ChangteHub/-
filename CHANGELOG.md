# Changelog

本文件记录校园二手交易平台（project1）的版本演进。遵循 Conventional Commits；架构决策见 docs/architecture/decision-record.md。

## [Unreleased] — 第二轮重构（Skill v3.0 REFACTOR 模式）

### Added
- `docs/architecture/decision-record.md`：架构决策记录文件化（Skill v3.0 契约）
- `docs/api.md`：离线 API 摘要（生产环境 Knife4j 关闭后的文档兜底）
- `backend/src/main/resources/logback-spring.xml`：统一日志格式 + 按天滚动（100MB/30 天/3GB 上限）+ 异步落盘
- `frontend/src/constants/`：状态与字典常量模块（PRODUCT_STATUS / USER_STATUS / CATEGORY_STATUS / VERIFICATION_STATUS / PRODUCT_CONDITIONS）
- 项目级 `CHANGELOG.md` 与 `LICENSE`（MIT）

### Changed
- Admin 三页（商品/用户/分类/认证管理）与发布页：状态魔法值（`status === 0 ? '正常' : '已禁用'` 等）与成色选项收敛到 constants/，消除 6 处重复定义

### 保持不变（REFACTOR 显式决策）
- 前端不抽 layouts/（BottomTab 已是唯一全局布局）、不建 assets/（无构建期静态资源）
- Husky/ESLint 继续暂缓；数据访问层保持 repository/（MyBatis-Plus）；不加 MapStruct

## [2.0.0] - 2026-08-30 — 首轮企业级重构

### Changed
- 目录 `back/ → backend/`；后端包规范化：security/（JWT 三件）、exception/（全局异常）、dto/request + dto/response（XxxRequest/XxxResponse）、repository/（数据访问层）
- 数据库迁移 Flyway 化：V1 表结构 / V2 初始数据 / V3 补齐统一字段；存量库 baseline-version=2 接入
- 配置三环境拆分（application-{dev,prod}.yml）；prod 密钥 fail-fast、关闭 API 文档；JDK 17→21
- 前端 services/ 分域拆分（request.ts + 11 个业务模块）、store/ → stores/、新增 @ 路径别名
- 新增后端 10 个单元/切片测试、前端 Vitest 8 用例

### Added
- Docker：前后端多阶段 Dockerfile + Nginx 反代（/api /uploads /ws）+ docker-compose 安全基线
- CI：GitHub Actions（前端检查/测试/构建 + 后端测试/打包）
- 工程化：docs/ 四篇、scripts/ 三个运维脚本、Makefile、.env.example、.editorconfig/.gitattributes

## [1.0.0] - 2026-08-17 — 初始版本

- 校园二手交易平台完整实现：注册登录(JWT)、商品发布/搜索/收藏、站内聊天(WebSocket)、实名认证、管理后台、深色模式
