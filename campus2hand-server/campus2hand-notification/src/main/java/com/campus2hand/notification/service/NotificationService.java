package com.campus2hand.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.notification.entity.Notification;
import com.campus2hand.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    public PageResponse<Notification> getNotifications(int page, int size, Boolean unread) {
        Long userId = CurrentUserHolder.getUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (Boolean.TRUE.equals(unread)) {
            wrapper.eq(Notification::getIsRead, false);
        }
        wrapper.orderByDesc(Notification::getIsRead);
        wrapper.orderByDesc(Notification::getCreatedAt);

        Page<Notification> result = notificationMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void markRead(Long notificationId) {
        Long userId = CurrentUserHolder.getUserId();
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND, "通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作");
        }
        notification.setIsRead(true);
        notificationMapper.updateById(notification);
    }

    @Transactional
    public void markAllRead() {
        Long userId = CurrentUserHolder.getUserId();
        List<Notification> unreadList = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
        );
        for (Notification n : unreadList) {
            n.setIsRead(true);
            notificationMapper.updateById(n);
        }
    }

    public long getUnreadCount() {
        Long userId = CurrentUserHolder.getUserId();
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, false)
        );
    }

    @Transactional
    public void createNotification(Long userId, String type, String title, String content,
                                   String targetType, Long targetId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notificationMapper.insert(notification);
        log.info("Notification created: userId={}, type={}, title={}", userId, type, title);
    }
}