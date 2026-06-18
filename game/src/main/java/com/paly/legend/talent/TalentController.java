package com.paly.legend.talent;

import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/talents")
public class TalentController {

    private final TalentService talentService;

    public TalentController(TalentService talentService) {
        this.talentService = talentService;
    }

    @GetMapping
    public ApiResponse<TalentListResponse> list() {
        return ApiResponse.ok(talentService.list(AuthContext.getRequired()));
    }

    @PostMapping("/upgrade")
    public ApiResponse<TalentListResponse> upgrade(@Valid @RequestBody TalentActionRequest request) {
        return ApiResponse.ok(talentService.upgrade(AuthContext.getRequired(), request));
    }

    @PostMapping("/reset")
    public ApiResponse<TalentListResponse> reset() {
        return ApiResponse.ok(talentService.reset(AuthContext.getRequired()));
    }
}
