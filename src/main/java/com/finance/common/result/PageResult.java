package com.finance.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页响应统一返回类
 * @param <T>
 */
@Data
public class PageResult<T> {

    private long total;// 总记录数
    private int page;// 当前页码
    private int size;// 每页条数
    private int pages;// 总页数
    private List<T> records;// 当前页的数据列表

    public PageResult() {}

    /**
     * 总记录数，当前页码，每页条数，数据列表，总页数自动计算
     * @param total
     * @param page
     * @param size
     * @param records
     */
    public PageResult(long total, int page, int size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        // 自动计算总页数：总数 ÷ 每页条数，向上取整
        this.pages = (int) Math.ceil((double) total / size);
        this.records = records;
    }
}
