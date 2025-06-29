package com.nyx.visitorcounter.model;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装类
 * 用于封装分页查询的结果数据，包括数据列表、总记录数、页码等信息
 * @param <T> 数据类型泛型参数，可以是任意实体类型
 */
@Data
public class PageResult<T> {
    private List<T> list; // 数据列表
    private long total; // 总记录数
    private int pageNum; // 当前页码
    private int pageSize; // 每页记录数
    private int pages; // 总页数
    
    /**
     * 构造分页结果对象
     * 
     * @param list 当前页的数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @param pages 总页数
     */
    public PageResult(List<T> list, long total, int pageNum, int pageSize, int pages) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }
}