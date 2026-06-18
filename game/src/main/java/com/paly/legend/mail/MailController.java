package com.paly.legend.mail;

import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mails")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping
    public ApiResponse<List<MailResponse>> list(@RequestParam(defaultValue = "30") int limit) {
        return ApiResponse.ok(mailService.list(AuthContext.getRequired(), limit));
    }

    @PostMapping("/{mailId}/claim")
    public ApiResponse<MailClaimResponse> claim(@PathVariable long mailId) {
        return ApiResponse.ok(mailService.claim(AuthContext.getRequired(), mailId));
    }

    @PostMapping("/{mailId}/read")
    public ApiResponse<Void> read(@PathVariable long mailId) {
        mailService.markRead(AuthContext.getRequired(), mailId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{mailId}/delete")
    public ApiResponse<Void> delete(@PathVariable long mailId) {
        mailService.delete(AuthContext.getRequired(), mailId);
        return ApiResponse.ok(null);
    }
}
