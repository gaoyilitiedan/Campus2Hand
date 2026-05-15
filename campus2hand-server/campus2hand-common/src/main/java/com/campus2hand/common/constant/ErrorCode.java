package com.campus2hand.common.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "成功"),
    PARAM_INVALID(1001, "参数校验失败"),
    RESOURCE_NOT_FOUND(1002, "资源不存在"),
    NO_PERMISSION(1003, "无权限"),
    TOKEN_INVALID(1004, "Token 过期或无效"),
    RATE_LIMITED(1005, "请求过于频繁"),
    USER_NOT_VERIFIED(2001, "用户未实名认证"),
    ACCOUNT_LOCKED(2002, "账户已锁定"),
    PAY_FAILED(3001, "支付失败"),
    ORDER_STATUS_ERROR(3002, "订单状态不合法"),
    INTERNAL_ERROR(5000, "服务器内部错误"),

    USER_NOT_FOUND(1010, "用户不存在"),
    PASSWORD_WRONG(1011, "密码错误"),
    ACCOUNT_DISABLED(1012, "账号已被禁用"),
    TOKEN_EXPIRED(1013, "Token已过期"),
    REFRESH_TOKEN_INVALID(1014, "RefreshToken无效或已过期"),
    STUDENT_ID_EXISTS(1015, "该学号已注册"),
    EMAIL_EXISTS(1016, "该邮箱已注册"),
    EMAIL_VERIFY_FAILED(1017, "邮箱验证失败"),
    VERIFY_CODE_EXPIRED(1018, "验证码已过期"),
    VERIFY_CODE_WRONG(1019, "验证码错误"),
    OLD_PASSWORD_WRONG(1020, "旧密码错误"),
    EMAIL_TOKEN_EXPIRED(1021, "邮箱验证链接已过期"),
    EMAIL_TOKEN_USED(1022, "邮箱验证链接已使用"),
    EMAIL_SEND_LIMIT(1023, "邮件发送次数已达上限"),
    CAMPUS_VERIFY_FAILED(1024, "校园身份校验失败"),

    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_NOT_ON_SALE(2002, "商品已下架或已售出"),
    PRODUCT_AUDIT_REJECTED(2003, "商品审核未通过"),
    IMAGE_UPLOAD_FAILED(2004, "图片上传失败"),
    IMAGE_COUNT_EXCEED(2005, "图片数量超出限制"),
    SENSITIVE_WORD_DETECTED(2006, "内容包含敏感词"),
    PRODUCT_LIMIT_EXCEED(2007, "在售商品数量已达上限"),

    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_INVALID(3002, "订单状态不允许此操作"),
    ORDER_CANNOT_CANCEL(3003, "订单无法取消"),
    ORDER_CANNOT_REFUND(3004, "订单无法退款"),
    PAYMENT_FAILED(3005, "支付失败"),
    REFUND_FAILED(3006, "退款失败"),
    CANNOT_BUY_OWN(3007, "不能购买自己的商品"),
    ORDER_EXPIRED(3008, "订单已过期"),

    CONVERSATION_NOT_FOUND(4001, "会话不存在"),
    MESSAGE_SEND_FAILED(4002, "消息发送失败"),

    REVIEW_NOT_FOUND(5001, "评价不存在"),
    ALREADY_REVIEWED(5002, "已评价过此订单"),
    REVIEW_NOT_ALLOWED(5003, "当前不允许评价"),

    DISPUTE_NOT_FOUND(6001, "工单不存在"),
    DISPUTE_STATUS_INVALID(6002, "工单状态不允许此操作"),
    DISPUTE_ALREADY_EXISTS(6003, "该订单已有进行中的工单"),

    NOTIFICATION_NOT_FOUND(7001, "通知不存在"),

    ADMIN_PERMISSION_DENIED(8001, "需要管理员权限"),
    ADMIN_AUDIT_REASON_REQUIRED(8002, "驳回时必须填写原因");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}