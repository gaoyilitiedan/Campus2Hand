package com.campus2hand.chat.controller;

import com.campus2hand.chat.dto.ConversationResponse;
import com.campus2hand.chat.dto.MessageResponse;
import com.campus2hand.chat.dto.SendMessageRequest;
import com.campus2hand.chat.service.ChatService;
import com.campus2hand.common.dto.ApiResponse;
import com.campus2hand.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ApiResponse<ConversationResponse> getOrCreateConversation(@RequestParam Long productId) {
        return ApiResponse.success(chatService.getOrCreateConversation(productId));
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ConversationResponse>> getConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(chatService.getConversations(page, size));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ApiResponse<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.success(chatService.sendMessage(conversationId, request.getContent(), request.getType()));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<PageResponse<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(chatService.getMessages(conversationId, page, size));
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long conversationId) {
        chatService.markRead(conversationId);
        return ApiResponse.success();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount() {
        return ApiResponse.success(chatService.getUnreadCount());
    }
}