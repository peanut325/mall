package com.atguigu.gulimall.gulimallsearch.service;

import com.atguigu.gulimall.gulimallsearch.vo.SearchParam;
import com.atguigu.gulimall.gulimallsearch.vo.SearchResult;

public interface ElasticSearchService {
    /**
     * 检索商品服务
     *
     * @return
     */
    SearchResult search(SearchParam searchParam);
}
