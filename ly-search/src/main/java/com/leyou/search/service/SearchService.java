package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.*;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 18:32
 */
@Slf4j
@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Goods buildGoods(Spu spu) {
        // 查询分类
        List<Category> categories = categoryClient.queryCategoryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 搜索字段
        String all = spu.getTitle() + StringUtils.join(categoryNames,",") + brand.getName();

        // 查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        // 对sku进行处理，获取部分字段
        List<Map<String, Object>> skus = new ArrayList<>();
        // 所有sku价格的集合
        List<Long> priceList = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            // 存入
            skus.add(map);
            // 存入价格集合
            priceList.add(sku.getPrice());
        }

        // 查询spu下的所有规格参数
        List<SpecParam> specParamList = specificationClient.queryParamList(null, spu.getCid3(), true);
        // 查询detail
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        // 获取通用规格参数
        Map<Long, String> genericSpecParamMap = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        // 获取特定规格参数
        Map<Long, List<String>> specialSpecParamMap = JsonUtils.
                nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        // 规格参数，key是规格参数的名，value是对应的值
        Map<String, Object> specParamMap = new HashMap<>();
        for (SpecParam specParam : specParamList) {
            String key = specParam.getName();
            // 获取规格参数的id
            Long specPramId = specParam.getId();
            Object value;
            // 是否是通用规格参数
            if (specParam.getGeneric()) {
                value = genericSpecParamMap.get(specPramId);
                // 是否要分段
                if (specParam.getNumeric()) {
                    value = chooseSegment(value.toString(), specParam);
                }
            } else {
                value = specialSpecParamMap.get(specPramId);
            }
            // 存入map
            specParamMap.put(key, value);
        }

        // 构建Goods对象
        Goods goods = new Goods();
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);// TODO 所有需要被搜索的信息，标题，分类，品牌，规格等
        goods.setPrice(priceList);// TODO 所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skus));// TODO 所有sku集合的json格式
        goods.setSpecs(specParamMap);// TODO 所有可搜索的规格参数,key是参数名,值是参数值
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest searchRequest) {
        // 1.创建查询构造器对象
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        // 2.分页
        builder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));
        // 3.结果过滤
        builder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 4.添加查询条件
        QueryBuilder basicQueryBuilder = buildBasicQuery(searchRequest);
        builder.withQuery(basicQueryBuilder);
        // 5.聚合分类和品牌
        // 5.1 聚合分类
        String categoryAggName = "category_agg";
        builder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 5.2 聚合品牌
        String brandAggName = "brand_agg";
        builder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 6.解析聚合结果
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(builder.build(), Goods.class);
        // 6.1 解析分页结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        // 6.2 解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAggregation(categoryAggName, aggs);
        List<Brand> brands = parseBrandAggregation(brandAggName, aggs);
        // 7.聚合规格参数
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 商品分类数量为1的时候可以直接聚合规格参数
            specs = buildSpecParamAggregation(categories.get(0).getId(),basicQueryBuilder);
        }
        return SearchResult.build(total, totalPage, goodsList, brands, categories,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        // 创建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 添加查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        // 获取过滤条件
        Map<String, String> filter = searchRequest.getFilter();
        // 添加过滤条件
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 如果不是品牌或分类，需要使用keyword字段来进行term匹配
            if (!"brandId".equals(key) && !"cid3".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, value));
        }
        return boolQueryBuilder;
    }

    private List<Map<String, Object>> buildSpecParamAggregation(Long cid, QueryBuilder basicQueryBuilder) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 1.查询规格参数
        List<SpecParam> paramList = specificationClient.queryParamList(null, cid, true);
        // 2.使用规格参数聚合
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withQuery(basicQueryBuilder);
        for (SpecParam specParam : paramList) {
            String name = specParam.getName();
            builder.addAggregation(AggregationBuilders.terms(name).
                    field("specs." + name + ".keyword"));
        }
        // 3.获取聚合结果
        AggregatedPage<Goods> result = elasticsearchTemplate.queryForPage(builder.build(), Goods.class);
        // 4.解析聚合结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam specParam : paramList) {
            StringTerms agg = aggs.get(specParam.getName());
            List<String> options = agg.getBuckets().stream().map(StringTerms.Bucket::getKeyAsString).
                    filter(StringUtils::isNotBlank).collect(Collectors.toList());

            Map<String, Object> map = new HashMap<>();
            map.put("k",specParam.getName());
            map.put("options", options);

            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAggregation(String brandAggName, Aggregations aggs) {
        try {
            LongTerms brandTerms = aggs.get(brandAggName);
            List<Long> ids = brandTerms.getBuckets().stream()
                    .map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return brandClient.queryBrandListByIds(ids);
        } catch (Exception e) {
            log.error("[搜索服务]查询品牌异常:", e);
            return null;
        }
    }

    private List<Category> parseCategoryAggregation(String categoryAggName, Aggregations aggs) {
        try {
            LongTerms categoryTerms = aggs.get(categoryAggName);
            List<Long> ids = categoryTerms.getBuckets().stream()
                    .map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return categoryClient.queryCategoryListByIds(ids);
        } catch (Exception e) {
            log.error("[搜索服务]查询分类异常:", e);
            return null;
        }
    }

    public void insertOrUpdateIndex(Long spuId) {
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 构建goods对象
        Goods goods = buildGoods(spu);
        // 新增或者保存
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
