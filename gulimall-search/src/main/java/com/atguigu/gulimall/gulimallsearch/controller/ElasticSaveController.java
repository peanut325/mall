package com.atguigu.gulimall.gulimallsearch.controller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallsearch.service.ElasticSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ElasticSaveService elasticSaveService;

    /**
     * 保存商品到ES中
     *
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean flag = false;
        try {
            flag = elasticSaveService.productStatusUp(skuEsModelList);
        } catch (IOException e) {
            // 抛出异常
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        // false表示没有错误
        return flag ? R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg()) : R.ok();
    }

}
