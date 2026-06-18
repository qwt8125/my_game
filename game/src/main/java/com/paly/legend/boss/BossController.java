package com.paly.legend.boss;

import java.util.List;

import com.paly.legend.battle.BattleSessionResponse;
import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bosses")
public class BossController {

    private final BossService bossService;

    public BossController(BossService bossService) {
        this.bossService = bossService;
    }

    @GetMapping
    public ApiResponse<List<BossResponse>> list() {
        return ApiResponse.ok(bossService.list());
    }

    @PostMapping("/{bossId}/fight")
    public ApiResponse<BossFightResponse> fight(@PathVariable String bossId) {
        return ApiResponse.ok(bossService.fight(AuthContext.getRequired(), bossId));
    }

    @PostMapping("/{bossId}/start")
    public ApiResponse<BattleSessionResponse> start(@PathVariable String bossId) {
        return ApiResponse.ok(bossService.start(AuthContext.getRequired(), bossId));
    }
}
