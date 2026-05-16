package com.campus2hand.order.controller;

import com.campus2hand.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/callback")
@RequiredArgsConstructor
public class PayCallbackController {

    private final OrderService orderService;

    @PostMapping("/wechat-pay")
    public Map<String, String> wechatPayCallback(
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestBody String body) {
        log.info("WeChat pay callback received: signature={}, nonce={}", signature, nonce);
        try {
            orderService.handlePayCallback(body);
            return Map.of("code", "SUCCESS");
        } catch (Exception e) {
            log.error("WeChat pay callback processing failed", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }
}