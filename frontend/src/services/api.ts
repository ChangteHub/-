// 兼容出口：保持既有 `from '../services/api'` 引用可用。
// 新代码请直接从 '@/services' 引入（分域模块见 auth.ts / product.ts / chat.ts 等）。
export * from './index'
export { default } from './request'
