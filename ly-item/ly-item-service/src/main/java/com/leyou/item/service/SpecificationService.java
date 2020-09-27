package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/20 9:47
 */
public interface SpecificationService {
    List<SpecGroup> queryGroupByCid(Long cid);


    List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching);

    List<SpecGroup> querySpecGroupListWithSpecPram(Long cid);
}
