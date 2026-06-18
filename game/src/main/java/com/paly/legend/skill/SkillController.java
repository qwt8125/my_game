package com.paly.legend.skill;

import java.util.List;
import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ApiResponse<List<SkillResponse>> list() {
        return ApiResponse.ok(skillService.list(AuthContext.getRequired()));
    }

    @PostMapping("/learn")
    public ApiResponse<List<SkillResponse>> learn(@Valid @RequestBody SkillActionRequest request) {
        return ApiResponse.ok(skillService.learn(AuthContext.getRequired(), request));
    }

    @PostMapping("/upgrade")
    public ApiResponse<List<SkillResponse>> upgrade(@Valid @RequestBody SkillActionRequest request) {
        return ApiResponse.ok(skillService.upgrade(AuthContext.getRequired(), request));
    }

    @PostMapping("/slot")
    public ApiResponse<List<SkillResponse>> updateSlot(@Valid @RequestBody SkillSlotRequest request) {
        return ApiResponse.ok(skillService.updateSlot(AuthContext.getRequired(), request));
    }
}
