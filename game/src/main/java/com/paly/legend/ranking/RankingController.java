package com.paly.legend.ranking;

import java.util.List;

import com.paly.legend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/level")
    public ApiResponse<List<RankingEntryResponse>> level(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.level(limit));
    }

    @GetMapping("/level/snapshot")
    public ApiResponse<RankingSnapshotResponse> levelSnapshot(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.levelSnapshot(limit));
    }

    @GetMapping("/power")
    public ApiResponse<List<RankingEntryResponse>> power(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.power(limit));
    }

    @GetMapping("/power/snapshot")
    public ApiResponse<RankingSnapshotResponse> powerSnapshot(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.powerSnapshot(limit));
    }

    @GetMapping("/gold")
    public ApiResponse<List<RankingEntryResponse>> gold(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.gold(limit));
    }

    @GetMapping("/gold/snapshot")
    public ApiResponse<RankingSnapshotResponse> goldSnapshot(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(rankingService.goldSnapshot(limit));
    }
}
