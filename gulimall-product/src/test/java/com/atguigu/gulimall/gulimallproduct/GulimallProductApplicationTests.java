package com.atguigu.gulimall.gulimallproduct;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = GulimallProductApplicationTests.class)
@ComponentScan(basePackages = "com.atguigu")
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
//        BrandEntity brand = new BrandEntity();
//        brand.setDescript("康师傅");
//        brandService.save(brand);
        BrandEntity brand = brandService.getById("1");
        System.out.println(brand);
    }

}
