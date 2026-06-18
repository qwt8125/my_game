package com.paly.legend.activity;

import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ApiResponse<List<ActivityResponse>> list() {
        return ApiResponse.ok(activityService.list(AuthContext.getRequired()));
    }

    @PostMapping("/{activityId}/claim")
    public ApiResponse<ActivityClaimResponse> claim(@PathVariable String activityId) {
        return ApiResponse.ok(activityService.claim(AuthContext.getRequired(), activityId));
    }
}
