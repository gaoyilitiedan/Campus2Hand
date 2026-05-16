package com.campus2hand.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.entity.User;
import com.campus2hand.auth.mapper.UserMapper;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.chat.dto.ConversationResponse;
import com.campus2hand.chat.dto.MessageResponse;
import com.campus2hand.chat.entity.Conversation;
import com.campus2hand.chat.entity.Message;
import com.campus2hand.chat.mapper.ConversationMapper;
import com.campus2hand.chat.mapper.MessageMapper;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.product.entity.Product;
import com.campus2hand.product.entity.ProductImage;
import com.campus2hand.product.mapper.ProductImageMapper;
import com.campus2hand.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;


    @Transactional
    public ConversationResponse getOrCreateConversation(Long productId) {
        Long userId = CurrentUserHolder.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null || product.getDeleted()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "不能与自己发起会话");
        }
        Conversation conv = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getProductId, productId)
                        .eq(Conversation::getBuyerId, userId)
        );
        if (conv == null) {
            conv = new Conversation();
            conv.setProductId(productId);
            conv.setBuyerId(userId);
            conv.setSellerId(product.getUserId());
            conversationMapper.insert(conv);
        }
        return buildConversationResponse(conv);
    }

    public PageResponse<ConversationResponse> getConversations(int page, int size) {
        Long userId = CurrentUserHolder.getUserId();
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Conversation::getBuyerId, userId).or().eq(Conversation::getSellerId, userId));
        wrapper.orderByDesc(Conversation::getLastMessageAt);
        wrapper.orderByDesc(Conversation::getUpdatedAt);

        Page<Conversation> result = conversationMapper.selectPage(new Page<>(page, size), wrapper);
        List<ConversationResponse> records = result.getRecords().stream()
                .map(this::buildConversationResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Transactional
    public MessageResponse sendMessage(Long conversationId, String content, String type) {
        Long senderId = CurrentUserHolder.getUserId();
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, "会话不存在");
        }
        if (!conv.getBuyerId().equals(senderId) && !conv.getSellerId().equals(senderId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权在此会话发送消息");
        }
        Long receiverId = conv.getBuyerId().equals(senderId) ? conv.getSellerId() : conv.getBuyerId();

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        messageMapper.insert(message);

        conv.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        conv.setLastMessageAt(LocalDateTime.now());
        if (conv.getBuyerId().equals(senderId)) {
            conv.setSellerUnread(conv.getSellerUnread() + 1);
        } else {
            conv.setBuyerUnread(conv.getBuyerUnread() + 1);
        }
        conversationMapper.updateById(conv);

        return buildMessageResponse(message);
    }

    public PageResponse<MessageResponse> getMessages(Long conversationId, int page, int size) {
        Long userId = CurrentUserHolder.getUserId();
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, "会话不存在");
        if (!conv.getBuyerId().equals(userId) && !conv.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权查看此会话");
        }

        Page<Message> result = messageMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByDesc(Message::getCreatedAt)
        );
        List<MessageResponse> records = result.getRecords().stream()
                .map(this::buildMessageResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    @Transactional
    public void markRead(Long conversationId) {
        Long userId = CurrentUserHolder.getUserId();
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) throw new BusinessException(ErrorCode.CONVERSATION_NOT_FOUND, "会话不存在");

        List<Message> unreadMessages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .eq(Message::getReceiverId, userId)
                        .eq(Message::getIsRead, false)
        );
        for (Message msg : unreadMessages) {
            msg.setIsRead(true);
            msg.setReadAt(LocalDateTime.now());
            messageMapper.updateById(msg);
        }
        if (conv.getBuyerId().equals(userId)) {
            conv.setBuyerUnread(0);
        } else {
            conv.setSellerUnread(0);
        }
        conversationMapper.updateById(conv);
    }

    public int getUnreadCount() {
        Long userId = CurrentUserHolder.getUserId();
        List<Conversation> conversations = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .and(w -> w.eq(Conversation::getBuyerId, userId).or().eq(Conversation::getSellerId, userId))
        );
        return conversations.stream()
                .mapToInt(c -> c.getBuyerId().equals(userId) ? c.getBuyerUnread() : c.getSellerUnread())
                .sum();
    }

    private ConversationResponse buildConversationResponse(Conversation conv) {
        Long userId = CurrentUserHolder.getUserId();
        Long otherId = conv.getBuyerId().equals(userId) ? conv.getSellerId() : conv.getBuyerId();
        User other = userMapper.selectById(otherId);
        Product product = productMapper.selectById(conv.getProductId());
        String coverImage = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, conv.getProductId())
                        .orderByAsc(ProductImage::getSortOrder)
                        .last("LIMIT 1")
        ).stream().findFirst().map(ProductImage::getUrl).orElse(null);

        int unread = conv.getBuyerId().equals(userId) ? conv.getBuyerUnread() : conv.getSellerUnread();

        return ConversationResponse.builder()
                .id(conv.getId())
                .productId(conv.getProductId())
                .productTitle(product != null ? product.getTitle() : "已删除")
                .productCoverImage(coverImage)
                .buyerId(conv.getBuyerId())
                .sellerId(conv.getSellerId())
                .otherNickname(other != null ? other.getNickname() : "未知用户")
                .otherAvatarUrl(other != null ? other.getAvatarUrl() : null)
                .lastMessage(conv.getLastMessage())
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(unread)
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private MessageResponse buildMessageResponse(Message msg) {
        return MessageResponse.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .type(msg.getType())
                .isRead(msg.getIsRead())
                .readAt(msg.getReadAt())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}