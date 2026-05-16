package com.campus2hand.admin.dto;

import lombok.Data;

@Data
public class AuditRequest {
    private String action;
    private String reason;
}