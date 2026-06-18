package com.paly.legend.idle;

import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/idle")
public class IdleController {

    private final IdleService idleService;

    public IdleController(IdleService idleService) {
        this.idleService = idleService;
    }

    @GetMapping("/status")
    public ApiResponse<IdleStatusResponse> status() {
        return ApiResponse.ok(idleService.status(AuthContext.getRequired()));
    }

    @PostMapping("/start")
    public ApiResponse<IdleStatusResponse> start(@Valid @RequestBody IdleStartRequest request) {
        return ApiResponse.ok(idleService.start(AuthContext.getRequired(), request));
    }

    @PostMapping("/claim")
    public ApiResponse<IdleClaimResponse> claim() {
        return ApiResponse.ok(idleService.claim(AuthContext.getRequired()));
    }
}
