package com.swaply.userservice.client;

import com.swaply.userservice.dto.ApiResponse;
import com.swaply.userservice.dto.admin.message.ReportMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "chat-service", url = "http://chat-service:8080")
@Component
public interface ChatClient {
    @GetMapping("/chat/reported-messages")
    ApiResponse<List<ReportMessageDto>> getReportMessages();

    @PutMapping("/chat/reported/ban")
    void setBanned(@RequestParam("messageId") String messageId);

    @PutMapping("/chat/reported/resolved")
    void setResolved(@RequestParam("messageId") String messageId);
}
