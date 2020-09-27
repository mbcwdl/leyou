package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/24 10:30
 */
@Service
public class PageService {

    private final GoodsClient goodsClient;

    private final CategoryClient categoryClient;

    private final BrandClient brandClient;

    private final SpecificationClient specificationClient;

    private final TemplateEngine templateEngine;

    public PageService(GoodsClient goodsClient, CategoryClient categoryClient, BrandClient brandClient, SpecificationClient specificationClient, TemplateEngine templateEngine) {
        this.goodsClient = goodsClient;
        this.categoryClient = categoryClient;
        this.brandClient = brandClient;
        this.specificationClient = specificationClient;
        this.templateEngine = templateEngine;
    }

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> attributes = new HashMap<>();
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询detail
        SpuDetail detail = spu.getSpuDetail();
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询规格参数
        List<SpecGroup> specs = specificationClient.querySpecGroupListWithSpecPram(spu.getCid3());
        attributes.put("title", spu.getTitle());
        attributes.put("subTitle", spu.getSubTitle());
        attributes.put("skus", skus);
        attributes.put("detail", detail);
        attributes.put("brand", brand);
        attributes.put("categories", categories);
        attributes.put("specs", specs);
        return attributes;
    }

    public void createStaticPage(Long spuId) {
        deleteStaticPage(spuId);
        // 加载模型数据
        Context cxt = new Context();
        cxt.setVariables(loadModel(spuId));
        // 指定渲染后的页面所输出的位置
        String staticPagePath = getStaticPagePath(spuId);
        try(PrintWriter writer = new PrintWriter(staticPagePath, "UTF-8")) {
            // 使用模板引擎生成静态页
            templateEngine.process("item",cxt,writer);

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void deleteStaticPage(Long spuId) {
        // 获取静态页位置
        String filePath = getStaticPagePath(spuId);
        File f = new File(filePath);
        // 删除
        if (f.exists()) {
            f.delete();
        }
    }

    private String getStaticPagePath(Long spuId) {
        String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        return classPath + spuId +".html";
    }

}
