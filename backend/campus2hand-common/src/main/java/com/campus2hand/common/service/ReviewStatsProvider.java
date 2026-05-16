package com.campus2hand.common.service;

import com.campus2hand.common.dto.PageResponse;
import java.util.Map;

public interface ReviewStatsProvider {
    long countByTargetId(Long targetId);
}