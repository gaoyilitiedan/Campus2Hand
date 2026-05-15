package com.campus2hand.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus2hand.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}