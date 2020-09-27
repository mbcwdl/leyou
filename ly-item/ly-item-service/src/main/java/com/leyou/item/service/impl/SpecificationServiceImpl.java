package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/20 9:53
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;


    @Override
    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<SpecGroup> querySpecGroupListWithSpecPram(Long cid) {
        // 查询该分类下的所有规格参数组
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // 查询该分类下的所有规格参数
        List<SpecParam> specParams = queryParamList(null, cid, null);
        // 根据查出来的规格参数集合构建一个map，key是规格参数组的id，值是该组中的规格参数
        Map<Long, List<SpecParam>> map = new HashMap<>(16);
        for (SpecParam specParam : specParams) {
            Long gid = specParam.getGroupId();
            if (!map.containsKey(gid)) {
                map.put(gid, new ArrayList<>());
            }
            map.get(gid).add(specParam);
        }
        // 将规格参数集合填充到对应的规格参数组对象中去
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
