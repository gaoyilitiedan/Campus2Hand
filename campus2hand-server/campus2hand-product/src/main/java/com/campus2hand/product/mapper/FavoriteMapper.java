package com.campus2hand.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.product.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}