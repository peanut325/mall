package com.atguigu.gulimall.gulimallsearch.controller;

import com.atguigu.gulimall.gulimallsearch.service.ElasticSearchService;
import com.atguigu.gulimall.gulimallsearch.vo.SearchParam;
import com.atguigu.gulimall.gulimallsearch.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model, HttpServletRequest httpServletRequest) {
        searchParam.set_queryString(httpServletRequest.getQueryString());   // 获取查询请求
        SearchResult searchResult = elasticSearchService.search(searchParam);
        model.addAttribute("result", searchResult);
        return "list";
    }

}
