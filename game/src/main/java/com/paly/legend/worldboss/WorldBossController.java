package com.paly.legend.worldboss;

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
@RequestMapping("/api/world-bosses")
public class WorldBossController {

    private final WorldBossService worldBossService;

    public WorldBossController(WorldBossService worldBossService) {
        this.worldBossService = worldBossService;
    }

    @GetMapping
    public ApiResponse<List<WorldBossResponse>> list() {
        return ApiResponse.ok(worldBossService.list());
    }

    @GetMapping("/{bossId}/rankings")
    public ApiResponse<List<WorldBossDamageRankResponse>> rankings(@PathVariable String bossId) {
        return ApiResponse.ok(worldBossService.rankings(bossId));
    }

    @PostMapping("/{bossId}/start")
    public ApiResponse<BattleSessionResponse> start(@PathVariable String bossId) {
        return ApiResponse.ok(worldBossService.start(AuthContext.getRequired(), bossId));
    }
}
