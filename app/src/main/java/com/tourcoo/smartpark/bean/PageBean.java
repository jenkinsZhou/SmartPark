package com.tourcoo.smartpark.bean;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description : 分页实体
 * @company :途酷科技
 * @date 2020年12月09日11:11
 * @Email: 971613168@qq.com
 */
public class PageBean<T> {
    private int count;
    private int page;
    private int pageCount;
    private int perPage;
    private List<T> list;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
