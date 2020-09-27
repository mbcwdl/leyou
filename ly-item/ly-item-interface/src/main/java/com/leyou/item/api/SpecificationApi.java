package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/21 16:43
 */
public interface SpecificationApi {

    /**
     * 查询规格参数
     * @param gid 规格参数所属规格参数组
     * @param cid 规格参数所属分类
     * @param searching 该规格参数是否可搜索
     * @return 规格参数集合
     */
    @GetMapping("/spec/params")
    List<SpecParam> queryParamList(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 查询带有规格参数的规格参数组集合
     * @param cid 规格参数组所属分类
     * @return 规格参数组集合
     */
    @GetMapping("/spec/group")
    List<SpecGroup> querySpecGroupListWithSpecPram(@RequestParam("cid") Long cid);
}
