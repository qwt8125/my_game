package com.paly.legend.guild;

import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guilds")
public class GuildController {

    private final GuildService guildService;

    public GuildController(GuildService guildService) {
        this.guildService = guildService;
    }

    @GetMapping
    public ApiResponse<List<GuildSummaryResponse>> list(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(guildService.list(limit));
    }

    @GetMapping("/rankings")
    public ApiResponse<List<GuildRankingEntryResponse>> rankings(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(guildService.rankings(AuthContext.getRequired(), limit));
    }

    @GetMapping("/me")
    public ApiResponse<GuildDetailResponse> me() {
        return ApiResponse.ok(guildService.me(AuthContext.getRequired()));
    }

    @PostMapping
    public ApiResponse<GuildActionResponse> create(@RequestBody CreateGuildRequest request) {
        return ApiResponse.ok(guildService.create(AuthContext.getRequired(), request));
    }

    @PostMapping("/{guildId}/join")
    public ApiResponse<GuildActionResponse> join(@PathVariable long guildId) {
        return ApiResponse.ok(guildService.join(AuthContext.getRequired(), guildId));
    }

    @PostMapping("/leave")
    public ApiResponse<GuildActionResponse> leave() {
        return ApiResponse.ok(guildService.leave(AuthContext.getRequired()));
    }

    @PostMapping("/members/{characterId}/kick")
    public ApiResponse<GuildActionResponse> kick(@PathVariable long characterId) {
        return ApiResponse.ok(guildService.kick(AuthContext.getRequired(), characterId));
    }

    @PostMapping("/members/{characterId}/transfer")
    public ApiResponse<GuildActionResponse> transfer(@PathVariable long characterId) {
        return ApiResponse.ok(guildService.transfer(AuthContext.getRequired(), characterId));
    }

    @PostMapping("/donate")
    public ApiResponse<GuildActionResponse> donate(@RequestBody GuildDonateRequest request) {
        return ApiResponse.ok(guildService.donate(AuthContext.getRequired(), request));
    }

    @GetMapping("/shop")
    public ApiResponse<List<GuildShopItemResponse>> shop() {
        return ApiResponse.ok(guildService.shop(AuthContext.getRequired()));
    }

    @PostMapping("/shop/buy")
    public ApiResponse<GuildShopPurchaseResponse> buy(@RequestBody GuildShopPurchaseRequest request) {
        return ApiResponse.ok(guildService.buy(AuthContext.getRequired(), request));
    }

    @GetMapping("/activities")
    public ApiResponse<List<GuildActivityResponse>> activities() {
        return ApiResponse.ok(guildService.activities(AuthContext.getRequired()));
    }

    @PostMapping("/activities/{activityId}/claim")
    public ApiResponse<GuildActivityClaimResponse> claimActivity(@PathVariable String activityId) {
        return ApiResponse.ok(guildService.claimActivity(AuthContext.getRequired(), activityId));
    }
}
