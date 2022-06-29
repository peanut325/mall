package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catalog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2 组装成父子的树形结构
        List<CategoryEntity> level1Menus = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().longValue() == 0)   // 查出父节点为0的节点
                .map(menu -> {
                    // 调用方法设置children属性
                    menu.setChildren(getChildrens(menu, categoryEntities));
                    return menu;
                })
                // 降序排序，注意判空
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前的菜单是否被别的地方所引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCateLogPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        path = findParentPath(catelogId, path);
        // 由于收集的path是从子到父，所以进行逆序一下
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "category", key = "'getLevel1Category'"),
            @CacheEvict(value = "category", key = "'getCatalogJsonByDbBySpringCache'")
    })
//    @CacheEvict(value = "category", allEntries = true)  // 删除分区下的所有缓存
    public void updateDetails(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategoryDetails(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")    // 将数据保存到category分区，以方法名为key
    @Override
    public List<CategoryEntity> getLevel1Category() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    // 从缓存中获取分类
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        // 先从缓存中读取数据
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");

        // 缓存中没有数据，从数据库中查询
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catalog2VO>> catalogJsonByDb = getCatalogJsonByDb();
            // 转换为JSON字符串保存在缓存中
            String toJSONString = JSON.toJSONString(catalogJsonByDb);
            redisTemplate.opsForValue().set("catalogJson", toJSONString, 1, TimeUnit.DAYS);
            return catalogJsonByDb;
        }

        // 将保存在缓存中的信息转化为对象
        Map<String, List<Catalog2VO>> map = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2VO>>>() {
        });

        return map;
    }

    // 从数据库中获取分类
    public Map<String, List<Catalog2VO>> getCatalogJsonByDb() {
        // 查出所有一级分类
        List<CategoryEntity> level1Category = getLevel1Category();   // 防止多次查库

        // 将后面所有的查库先查出来，再通过getParentCid方法获取
        List<CategoryEntity> selectAllCategory = baseMapper.selectList(null);

        //封装数据
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 根据一级分类id，查询二级分类数据
            List<CategoryEntity> l2 = getParentCid(selectAllCategory, v.getCatId());
            // 封装二级分类数据
            List<Catalog2VO> catalog2VOList = new ArrayList<>();
            if (l2 != null) {
                catalog2VOList = l2.stream().map(categoryEntity -> {
                    Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, categoryEntity.getCatId().toString(), categoryEntity.getName());

                    // 根据二级分类查找三级分类，并封装
                    List<CategoryEntity> l3 = getParentCid(selectAllCategory, categoryEntity.getCatId());
                    List<Catalog2VO.Catalog3Vo> list = l3.stream().map(categoryEntity1 -> {
                        Catalog2VO.Catalog3Vo catalog3Vo = new Catalog2VO.Catalog3Vo(categoryEntity.getCatId().toString(), categoryEntity1.getCatId().toString(), categoryEntity1.getName());
                        return catalog3Vo;
                    }).collect(Collectors.toList());
                    catalog2VO.setCatalog3List(list);

                    return catalog2VO;
                }).collect(Collectors.toList());
            }

            return catalog2VOList;
        }));

        return map;
    }

    // 从缓存中获取分类(SpringCache的使用)
    @Override
    @Cacheable(value = {"category"}, key = "#root.method.name")    // 将数据保存到category分区，以方法名为key
    public Map<String, List<Catalog2VO>> getCatalogJsonByDbBySpringCache() {
        // 查出所有一级分类
        List<CategoryEntity> level1Category = getLevel1Category();   // 防止多次查库

        // 将后面所有的查库先查出来，再通过getParentCid方法获取
        List<CategoryEntity> selectAllCategory = baseMapper.selectList(null);

        // 封装数据
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 根据一级分类id，查询二级分类数据
            List<CategoryEntity> l2 = getParentCid(selectAllCategory, v.getCatId());
            // 封装二级分类数据
            List<Catalog2VO> catalog2VOList = new ArrayList<>();
            if (l2 != null) {
                catalog2VOList = l2.stream().map(categoryEntity -> {
                    Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, categoryEntity.getCatId().toString(), categoryEntity.getName());

                    // 根据二级分类查找三级分类，并封装
                    List<CategoryEntity> l3 = getParentCid(selectAllCategory, categoryEntity.getCatId());
                    List<Catalog2VO.Catalog3Vo> list = l3.stream().map(categoryEntity1 -> {
                        Catalog2VO.Catalog3Vo catalog3Vo = new Catalog2VO.Catalog3Vo(categoryEntity.getCatId().toString(), categoryEntity1.getCatId().toString(), categoryEntity1.getName());
                        return catalog3Vo;
                    }).collect(Collectors.toList());
                    catalog2VO.setCatalog3List(list);

                    return catalog2VO;
                }).collect(Collectors.toList());
            }

            return catalog2VOList;
        }));

        return map;
    }

    // 从数据库中获取，并存入redis中
    public Map<String, List<Catalog2VO>> getCatalogJsonByDbToRedis() {
        // 查出所有一级分类
        List<CategoryEntity> level1Category = getLevel1Category();   // 防止多次查库

        // 将后面所有的查库先查出来，再通过getParentCid方法获取
        List<CategoryEntity> selectAllCategory = baseMapper.selectList(null);

        //封装数据
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 根据一级分类id，查询二级分类数据
            List<CategoryEntity> l2 = getParentCid(selectAllCategory, v.getCatId());
            // 封装二级分类数据
            List<Catalog2VO> catalog2VOList = new ArrayList<>();
            if (l2 != null) {
                catalog2VOList = l2.stream().map(categoryEntity -> {
                    Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, categoryEntity.getCatId().toString(), categoryEntity.getName());

                    // 根据二级分类查找三级分类，并封装
                    List<CategoryEntity> l3 = getParentCid(selectAllCategory, categoryEntity.getCatId());
                    List<Catalog2VO.Catalog3Vo> list = l3.stream().map(categoryEntity1 -> {
                        Catalog2VO.Catalog3Vo catalog3Vo = new Catalog2VO.Catalog3Vo(categoryEntity.getCatId().toString(), categoryEntity1.getCatId().toString(), categoryEntity1.getName());
                        return catalog3Vo;
                    }).collect(Collectors.toList());
                    catalog2VO.setCatalog3List(list);

                    return catalog2VO;
                }).collect(Collectors.toList());
            }

            return catalog2VOList;
        }));

        // 转换为JSON字符串保存在缓存中,查到了还需要放入缓存，防止放入缓存有延时，导致后面再次查库
        String toJSONString = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalogJson", toJSONString);
        return map;
    }

    // 本地锁版本(已经废弃，不能支持分布式)
    @Deprecated
    public Map<String, List<Catalog2VO>> getCatalogJsonByDbWithLocalLock() {
        // 本地锁：synchronized，JUC(lock)，在分布式0情况下，需要使用分布式锁
        synchronized (this) {
            // 得到锁以后还要检查一次，double check
            Map<String, List<Catalog2VO>> catalogJsonByDb = getCatalogJsonByDb();
            // 转换为JSON字符串保存在缓存中,查到了还需要放入缓存，防止放入缓存有延时，导致后面再次查库
            String toJSONString = JSON.toJSONString(catalogJsonByDb);
            redisTemplate.opsForValue().set("catalogJson", toJSONString);
            return catalogJsonByDb;
        }
    }

    // 分布式锁版本(原生的set实现)
    @Deprecated
    public Map<String, List<Catalog2VO>> getCatalogJsonByDbWithRedisLock() {
        // 加锁去占坑
        String uuid = UUID.randomUUID().toString();
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (ifAbsent) {
            // 设置过期时间为30s,废弃，需要和加锁一起有原子性
            // redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            // 加锁完成，执行业务
            Map<String, List<Catalog2VO>> result;
            try {
                result = getCatalogJsonByDbToRedis();
            } finally {


                // 获取值和删除应该具有原子性
                // String lockValue = redisTemplate.opsForValue().get("lock"); // 获取锁的值
                // if (uuid.equals(lockValue)) {
                // 删除自己的锁
                //    redisTemplate.delete("lock"); // 释放锁
                // }

                // 查询UUID是否是自己，是自己的lock就删除
                // 封装lua脚本（原子操作解锁）
                // 查询+删除（当前值与目标值是否相等，相等执行删除，不等返回0）
                String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call('del',KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                // 执行lua脚本,lock相当于赋值给脚本的KEYS[1]，uuid相当于赋值给ARGV[1]
                // 删除成功返回1，失败返回0
                Long isDelete = redisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class),
                        Arrays.asList("lock"), uuid);

            }
            return result;
        } else {
            // 加锁失败，重试
            // 休眠200ms重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonByDbWithRedisLock();
        }
    }

    // 分布式锁版本(Redissson实现)
    public Map<String, List<Catalog2VO>> getCatalogJsonByDbWithRedissonLock() {
        // 锁的名字。锁的粒度，越细越快。
        // 锁的粒度:具体缓存的是某个数据，11-号商品;product-11-lock product-12-lockproduct-Lock
        RLock rLock = redissonClient.getLock("catalogJson");
        rLock.lock();   // 加锁

        Map<String, List<Catalog2VO>> result;
        try {
            result = getCatalogJsonByDbToRedis();
        } finally {
            rLock.unlock(); // 解锁
        }
        return result;
    }

    // 获取几级分类的内容
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectAllCategory, Long parentCid) {
        List<CategoryEntity> collect = selectAllCategory.stream().filter(categoryEntity -> categoryEntity.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }

    // 递归查询分类路径
    public List<Long> findParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) { // 循环找到最顶层的分类
            findParentPath(byId.getParentCid(), path);
        }
        return path;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                // 查出与父分类id相同的子分类的pid
                // 注意此处应该用longValue()来比较，否则会出先bug，因为parentCid和catId是long类型
                .filter(categoryEntity -> root.getCatId().longValue() == categoryEntity.getParentCid().longValue())
                .map(categoryEntity -> {
                    // 递归调用
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                })
                // 降序排序
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return children;
    }

}