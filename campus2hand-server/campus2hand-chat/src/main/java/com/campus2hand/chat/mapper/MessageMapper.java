package com.campus2hand.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}