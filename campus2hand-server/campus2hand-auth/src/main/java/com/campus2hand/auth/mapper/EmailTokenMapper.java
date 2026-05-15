package com.campus2hand.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.auth.entity.EmailToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailTokenMapper extends BaseMapper<EmailToken> {
}