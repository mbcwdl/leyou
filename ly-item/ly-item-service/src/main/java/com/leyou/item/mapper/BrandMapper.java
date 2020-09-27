package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 23:59
 */
public interface BrandMapper extends BaseMapper<Brand> {

    @Insert("insert into tb_category_brand(category_id, brand_id) values(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Select("SELECT a.* from tb_brand a inner join tb_category_brand b on a.id = b.brand_id where b.category_id = #{cid}")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
