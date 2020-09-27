package com.leyou.search.pojo;

import java.util.Map;

/** 接收前台传输的搜索参数
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/22 8:31
 */
public class SearchRequest {
    private String key;
    private Integer page;
    private Map<String, String> filter;

    private static final Integer DEFAULT_PAGE = 1;
    private static final Integer DEFAULT_SIZE = 20;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if (page == null) {
            return SearchRequest.DEFAULT_PAGE;
        }
        return Math.max(this.page,SearchRequest.DEFAULT_PAGE);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return SearchRequest.DEFAULT_SIZE;
    }

    public Map<String, String> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, String> filter) {
        this.filter = filter;
    }
}
