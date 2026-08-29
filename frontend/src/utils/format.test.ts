import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatPrice, formatChatTime } from './format'

describe('formatPrice', () => {
  it('保留两位小数并带人民币符号', () => {
    expect(formatPrice(25)).toBe('¥25.00')
    expect(formatPrice(980.5)).toBe('¥980.50')
  })
})

describe('formatDate', () => {
  it('空值返回占位符', () => {
    expect(formatDate(undefined)).toBe('-')
    expect(formatDate(null)).toBe('-')
  })

  it('格式化为中文日期', () => {
    expect(formatDate('2026-08-17T14:30:00')).toMatch(/^\d{4}\/\d{1,2}\/\d{1,2}$/)
  })
})

describe('formatChatTime', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-08-30T12:00:00'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('空值/非法值返回空字符串', () => {
    expect(formatChatTime(undefined)).toBe('')
    expect(formatChatTime('not-a-date')).toBe('')
  })

  it('今天显示 HH:mm', () => {
    expect(formatChatTime('2026-08-30T08:05:00')).toBe('08:05')
  })

  it('昨天显示"昨天 HH:mm"', () => {
    expect(formatChatTime('2026-08-29T08:05:00')).toBe('昨天 08:05')
  })

  it('一周内显示星期几', () => {
    // 2026-08-26 是周三
    expect(formatChatTime('2026-08-26T08:05:00')).toBe('周三 08:05')
  })

  it('更早显示完整日期', () => {
    expect(formatChatTime('2026-07-30T08:05:00')).toBe('2026/7/30')
  })
})
