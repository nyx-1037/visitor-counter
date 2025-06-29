package com.nyx.visitorcounter.model;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    private List<T> list; // 数据列表
    private long total; // 总记录数
    private int pageNum; // 当前页码
    private int pageSize; // 每页记录数
    private int pages; // 总页数
    
    public PageResult(List<T> list, long total, int pageNum, int pageSize, int pages) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }
}