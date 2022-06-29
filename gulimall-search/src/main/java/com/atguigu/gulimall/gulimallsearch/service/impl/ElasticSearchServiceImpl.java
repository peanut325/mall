package com.atguigu.gulimall.gulimallsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallsearch.client.ProductFeignClient;
import com.atguigu.gulimall.gulimallsearch.config.GulimallSearchConfig;
import com.atguigu.common.constant.es.EsConstant;
import com.atguigu.gulimall.gulimallsearch.service.ElasticSearchService;
import com.atguigu.gulimall.gulimallsearch.vo.AttrResponseVo;
import com.atguigu.gulimall.gulimallsearch.vo.BrandVo;
import com.atguigu.gulimall.gulimallsearch.vo.SearchParam;
import com.atguigu.gulimall.gulimallsearch.vo.SearchResult;
import com.alibaba.fastjson.TypeReference;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public SearchResult search(SearchParam searchParam) {
        // 动态构建查询需要的DSL语句
        SearchResult searchResult = null;

        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        // 执行检索请求
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, GulimallSearchConfig.COMMON_OPTIONS);

            // 封装返回结果
            searchResult = buildSearchResponse(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * 准备检索请求（参照dsl.json文件）
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();    // 构建DSL语句

        // 构建 bool-query,根据商品的标题进行检索
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }

        // 构建 bool-filter-term,根据三级分类进行检索
        if (!StringUtils.isEmpty(searchParam.getCatalog3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }

        // 构建 bool-filter-term,根据品牌id进行检索
        if (!StringUtils.isEmpty(searchParam.getBrandId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", searchParam.getBrandId()));
        }

        // bool-filter,根据属性值进行检索
        if (!StringUtils.isEmpty(searchParam.getAttrs())) {
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                // attr格式  attrs=1_白色:蓝色
                String[] attrs = attr.split("_");
                String attrId = attrs[0];
                String[] attrValues = attrs[1].split(":");// 再将多个属性值分开
                // TODO 此时如果属性只有一个，那么在ES中会封装为[高通]，此时会查不到数据
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                if (attrValues.length == 1) {
                    nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", attrValues[0]));
                } else {
                    // 多个就放入数组
                    nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", attrValues));
                }
                // 创建nested查询（注意每一个属性值，都需要一个nested进行查询）
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);   // 最后参数表示不参与评分
                boolQueryBuilder.filter(nestedQuery);
            }
        }

        // 构建 bool-filter-term,根据是否有库存进行检索
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));

        // bool-filter,根据价格区间进行查询
        // skuPrice=0_500/500_/_500【价格区间】
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String skuPrice = searchParam.getSkuPrice();
            String[] s = skuPrice.split("_");
            if (s.length == 2) {    // 指定了两边区间 0_500
                rangeQuery.gte(s[0]).lte(s[1]);
            } else { // 指定了一边区间 500_/_500
                if (skuPrice.endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }

                if (skuPrice.startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
            }

            boolQueryBuilder.filter(rangeQuery);
        }

        // 把以前所有的条件封装起来
        searchSourceBuilder.query(boolQueryBuilder);

        // 排序：sort=saleCount_asc
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] s = searchParam.getSort().split("_");
            SortOrder sortOrder = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], sortOrder);
        }

        // 分页
        // "from": 0,
        // "size": 2,
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 高亮
        // "highlight": {
        //    "fields": {
        //        "skuTitle": {}
        //    },
        //    "pre_tags": "<b style='color:red'>",
        //            "post_tags": "</b>"
        // }
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 聚合分析
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brand_agg);

        // 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalog_agg);

        // 属性聚合(nested属性)
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        searchSourceBuilder.aggregation(attr_agg);

        System.out.println(searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);

        return searchRequest;
    }

    /**
     * 封装返回结果（参照result.json文件）
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResponse(SearchResponse response, SearchParam searchParam) {

        SearchResult searchResult = new SearchResult();
        SearchHits hits = response.getHits();

        // 封装商品信息
        // 保存进去就是以SkuEsModel保存的
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        // hits的hits中的source保存的是商品信息
        for (SearchHit hit : response.getHits()) {
            // 获取到json数据，转换为对象
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
            // 设置高亮标题
            if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                String skuTitle = highlightFieldMap.get("skuTitle").getFragments()[0].string();
                skuEsModel.setSkuTitle(skuTitle);
            }
            skuEsModelList.add(skuEsModel);
        }
        searchResult.setProducts(skuEsModelList);

        // 当前商品所涉及的品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVoList = new ArrayList<>();
        List<? extends Terms.Bucket> brandList = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brandList) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            // 获取品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();

            // 获取品牌的名字
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            // 获取品牌的图片
            String brandImage = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImage);

            brandVoList.add(brandVo);
        }
        searchResult.setBrands(brandVoList);

        // 当前商品所涉及的属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        List<SearchResult.AttrVo> attrVoList = new ArrayList<>();
        ParsedLongTerms attrIdAgg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

            // 属性id
            long attrId = bucket.getKeyAsNumber().longValue();

            // 属性名
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();

            // 属性值
            List<String> attrValueList = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String attrValue = ((Terms.Bucket) item).getKeyAsString();
                return attrValue;
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValueList);

            attrVoList.add(attrVo);
        }
        searchResult.setAttrs(attrVoList);

        // 当前商品所涉及到的所有分类信息(从聚合中取出)
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVoList = new ArrayList<>();
        List<? extends Terms.Bucket> catalogList = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : catalogList) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            String catalogId = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogId));

            // 得到分类名，子聚合
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            List<? extends Terms.Bucket> catalogNameList = catalog_name_agg.getBuckets();
            String catalogName = catalogNameList.get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);

            catalogVoList.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVoList);

        // 封装分页信息
        long total = hits.getTotalHits().value;
        searchResult.setPageNum(searchParam.getPageNum());
        searchResult.setTotal(total);
        // 总页码（总页数 / 每页数）有余数加一
        int totalPage = (int) total % EsConstant.PRODUCT_PAGESIZE == 1 ? (((int) total / EsConstant.PRODUCT_PAGESIZE) + 1) : (int) total / EsConstant.PRODUCT_PAGESIZE;
        searchResult.setTotalPages(totalPage);

        // 导航页码[1、2、3、4、5]
        List<Integer> pageNavList = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavList.add(i);
        }
        searchResult.setPageNavs(pageNavList);

        // 构建面包屑导航功能
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVoList = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // attr=2_5寸:6寸(此时只有属性id和属性值)
                String[] split = attr.split("_");
                // 此时需要调用product服务查询属性名
                navVo.setNavValue(split[1]);
                R r = productFeignClient.attrInfo(Long.parseLong(split[0]));
                searchResult.getAttrIds().add(Long.parseLong(split[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    // 设置属性名
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(split[0]);
                }

                // 取消了这个面包屑以后，我们要跳转到那个地方.将请求地址的urL里面的当前置空拿到所有的查询条件，去掉当前等条件
                // attrs=15_海思(Hisilicon)
                String replace = replaceQueryString(searchParam, attr, "attrs");
                navVo.setLink("http://search.gulimalls.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVoList);
        }

        if (!StringUtils.isEmpty(searchParam.getBrandId())) {
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");

            // 远程查询所有品牌
            R r = productFeignClient.getInfoByBrandIds(searchParam.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brandVo = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer sb = new StringBuffer();
                String replace = null;
                for (BrandVo brand : brandVo) {
                    sb.append(brand.getBrandName() + ";");
                    replace = replaceQueryString(searchParam, brand.getBrandId().toString(), "brandId");
                }
                navVo.setNavValue(sb.toString());
                navVo.setLink("http://search.gulimalls.com/list.html?" + replace);
            }
            searchResult.getNavs().add(navVo);
        }

        // TODO 分类的面包屑，不需要导航取消

        return searchResult;
    }

    /**
     * 替换地址字符串
     *
     * @param searchParam
     * @param attr
     * @return
     */
    private String replaceQueryString(SearchParam searchParam, String attr, String key) {
        // TODO 请求地址参数转义有问题
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            encode = encode.replace("+", "%20"); // 处理空格
            encode = encode.replace("%28", "("); // 处理)
            encode = encode.replace("%29", ")"); // 处理(
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = searchParam.get_queryString().replace("&" + key + "=" + encode, " ");
        return replace;
    }


}
