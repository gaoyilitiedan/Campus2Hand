package com.campus2hand.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.user.entity.LoginHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginHistoryMapper extends BaseMapper<LoginHistory> {
}