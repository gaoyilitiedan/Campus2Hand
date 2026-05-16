package com.campus2hand.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}