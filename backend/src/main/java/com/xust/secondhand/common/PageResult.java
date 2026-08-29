package com.xust.secondhand.common;

import lombok.Data;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int pageNum;

    /** 每页大小 */
    private int pageSize;

    /** 总页数 */
    private int pages;

    /** 数据列表 */
    private List<T> list;

    public PageResult() {}

    public PageResult(long total, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        // 计算总页数（pageSize<=0时兜底为1，避免除零）
        int safePageSize = pageSize > 0 ? pageSize : 1;
        this.pages = (int) Math.ceil((double) total / safePageSize);
    }

    /**
     * 静态工厂方法
     */
    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }
}
