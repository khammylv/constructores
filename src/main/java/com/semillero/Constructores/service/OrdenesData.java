package com.semillero.Constructores.service;

import java.util.List;

public class OrdenesData<T> {
   private int PageIndex;
    private int PageSize;
    private long TotalCount;
    private int TotalPages;
    private List<T> Data;

    public int getPageIndex() {
        return PageIndex;
    }

    public void setPageIndex(int pageIndex) {
        PageIndex = pageIndex;
    }

    public int getPageSize() {
        return PageSize;
    }

    public void setPageSize(int pageSize) {
        PageSize = pageSize;
    }

    public long getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(long totalCount) {
        TotalCount = totalCount;
    }

    public int getTotalPages() {
        return TotalPages;
    }

    public void setTotalPages(int totalPages) {
        TotalPages = totalPages;
    }

    public List<T> getData() {
        return Data;
    }

    public void setData(List<T> data) {
        Data = data;
    }
}
