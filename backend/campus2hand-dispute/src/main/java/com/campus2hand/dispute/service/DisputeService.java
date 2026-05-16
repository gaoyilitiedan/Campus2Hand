package com.campus2hand.dispute.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus2hand.auth.security.CurrentUserHolder;
import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.dto.PageResponse;
import com.campus2hand.common.exception.BusinessException;
import com.campus2hand.dispute.dto.CreateDisputeRequest;
import com.campus2hand.dispute.dto.DisputeResponse;
import com.campus2hand.dispute.entity.Dispute;
import com.campus2hand.dispute.mapper.DisputeMapper;
import com.campus2hand.order.entity.Order;
import com.campus2hand.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeMapper disputeMapper;
    private final OrderMapper orderMapper;
    @Transactional
    public DisputeResponse create(CreateDisputeRequest request) {
        Long reporterId = CurrentUserHolder.getUserId();
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        if (!order.getBuyerId().equals(reporterId) && !order.getSellerId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权发起工单");
        }

        Dispute dispute = new Dispute();
        dispute.setOrderId(request.getOrderId());
        dispute.setReporterId(reporterId);
        dispute.setType(request.getType());
        dispute.setReason(request.getReason());
        dispute.setEvidenceUrls(request.getEvidenceUrls() != null ?
                String.join(",", request.getEvidenceUrls()) : null);
        dispute.setStatus("pending");
        disputeMapper.insert(dispute);
        return buildResponse(dispute);
    }

    public PageResponse<DisputeResponse> getMyDisputes(int page, int size) {
        Long userId = CurrentUserHolder.getUserId();
        Page<Dispute> result = disputeMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Dispute>()
                        .eq(Dispute::getReporterId, userId)
                        .orderByDesc(Dispute::getCreatedAt)
        );
        List<DisputeResponse> records = result.getRecords().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
        return PageResponse.of(records, result.getTotal(), page, size);
    }

    public DisputeResponse getDetail(Long disputeId) {
        Long userId = CurrentUserHolder.getUserId();
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) throw new BusinessException(ErrorCode.DISPUTE_NOT_FOUND, "工单不存在");
        if (!dispute.getReporterId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权查看此工单");
        }
        return buildResponse(dispute);
    }

    public void addComment(Long disputeId, String content) {
        Long userId = CurrentUserHolder.getUserId();
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) throw new BusinessException(ErrorCode.DISPUTE_NOT_FOUND, "工单不存在");
        if (!"pending".equals(dispute.getStatus()) && !"processing".equals(dispute.getStatus())) {
            throw new BusinessException(ErrorCode.DISPUTE_STATUS_INVALID, "当前状态不允许追加留言");
        }
        log.info("Dispute comment added: disputeId={}, userId={}", disputeId, userId);
    }

    private DisputeResponse buildResponse(Dispute dispute) {
        List<String> evidenceUrls = dispute.getEvidenceUrls() != null ?
                StrUtil.split(dispute.getEvidenceUrls(), ",") : List.of();

        return DisputeResponse.builder()
                .id(dispute.getId())
                .orderId(dispute.getOrderId())
                .reporterId(dispute.getReporterId())
                .type(dispute.getType())
                .reason(dispute.getReason())
                .evidenceUrls(evidenceUrls)
                .status(dispute.getStatus())
                .resolution(dispute.getResolution())
                .resolvedBy(dispute.getResolvedBy())
                .resolvedAt(dispute.getResolvedAt())
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }
}