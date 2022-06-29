package com.atguigu.gulimall.gulimallsearch.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.gulimallsearch.config.GulimallSearchConfig;
import com.atguigu.common.constant.es.EsConstant;
import com.atguigu.gulimall.gulimallsearch.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticSaveServiceImpl implements ElasticSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {

        // 给ES中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModelList) {
            // 创建索引对应的保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            indexRequest.source(JSONObject.toJSONString(model), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, GulimallSearchConfig.COMMON_OPTIONS);

        // TODO 如果批量出现错误
        boolean flag = bulkResponse.hasFailures(); // 响应体中检查是否出现错误

        List<String> idList = Arrays.stream(bulkResponse.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成:{}" + idList);

        return flag;
    }
}
