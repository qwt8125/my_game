package com.paly.legend.admin;

import java.util.List;

import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import com.paly.legend.config.ActivityConfig;
import com.paly.legend.mail.AdminSendMailRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/characters")
    public ApiResponse<List<AdminCharacterResponse>> listCharacters(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(adminService.listCharacters(AuthContext.getRequired(), limit));
    }

    @GetMapping("/activities")
    public ApiResponse<List<ActivityConfig>> listActivities() {
        return ApiResponse.ok(adminService.listActivities(AuthContext.getRequired()));
    }

    @PostMapping("/activities/{activityId}")
    public ApiResponse<AdminConfigReloadResponse> updateActivity(@PathVariable String activityId,
                                                                 @Valid @RequestBody ActivityConfig request) {
        return ApiResponse.ok(adminService.updateActivity(AuthContext.getRequired(), activityId, request));
    }

    @PostMapping("/grant-gold")
    public ApiResponse<AdminActionResponse> grantGold(@Valid @RequestBody AdminGrantGoldRequest request) {
        return ApiResponse.ok(adminService.grantGold(AuthContext.getRequired(), request));
    }

    @PostMapping("/grant-item")
    public ApiResponse<AdminActionResponse> grantItem(@Valid @RequestBody AdminGrantItemRequest request) {
        return ApiResponse.ok(adminService.grantItem(AuthContext.getRequired(), request));
    }

    @PostMapping("/send-mail")
    public ApiResponse<AdminActionResponse> sendMail(@Valid @RequestBody AdminSendMailRequest request) {
        return ApiResponse.ok(adminService.sendMail(AuthContext.getRequired(), request));
    }

    @GetMapping("/map-event-states")
    public ApiResponse<List<AdminMapEventStateResponse>> listMapEventStates(@RequestParam long characterId) {
        return ApiResponse.ok(adminService.listMapEventStates(AuthContext.getRequired(), characterId));
    }

    @PostMapping("/map-event-states/reset")
    public ApiResponse<AdminActionResponse> resetMapEventState(@Valid @RequestBody AdminMapEventResetRequest request) {
        return ApiResponse.ok(adminService.resetMapEventState(AuthContext.getRequired(), request));
    }

    @PostMapping("/map-event-states/reset-all")
    public ApiResponse<AdminActionResponse> resetAllMapEventStates(@Valid @RequestBody AdminMapEventResetAllRequest request) {
        return ApiResponse.ok(adminService.resetAllMapEventStates(AuthContext.getRequired(), request));
    }

    @PostMapping("/map-event-states/cleanup")
    public ApiResponse<AdminActionResponse> cleanupMapEventStates(@Valid @RequestBody AdminMapEventCleanupRequest request) {
        return ApiResponse.ok(adminService.cleanupMapEventStates(AuthContext.getRequired(), request));
    }

    @PostMapping("/account-status")
    public ApiResponse<AdminActionResponse> updateAccountStatus(@Valid @RequestBody AdminAccountStatusRequest request) {
        return ApiResponse.ok(adminService.updateAccountStatus(AuthContext.getRequired(), request));
    }

    @PostMapping("/config/reload")
    public ApiResponse<AdminConfigReloadResponse> reloadConfig() {
        return ApiResponse.ok(adminService.reloadConfig(AuthContext.getRequired()));
    }
}
