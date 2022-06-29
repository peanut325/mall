package com.atguigu.gulimall.gulimallsearch.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ElasticSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
