package com.paly.legend.map;

import java.util.ArrayList;
import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import com.paly.legend.config.GameConfigService;
import com.paly.legend.config.MapConfig;
import com.paly.legend.config.MonsterConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maps")
public class MapController {

    private final GameConfigService gameConfigService;
    private final MapInteractionService mapInteractionService;

    public MapController(GameConfigService gameConfigService, MapInteractionService mapInteractionService) {
        this.gameConfigService = gameConfigService;
        this.mapInteractionService = mapInteractionService;
    }

    @GetMapping
    public ApiResponse<List<MapSummaryResponse>> list() {
        List<MapSummaryResponse> result = new ArrayList<MapSummaryResponse>();
        for (MapConfig map : gameConfigService.listMaps()) {
            result.add(MapSummaryResponse.from(map));
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/{mapId}")
    public ApiResponse<MapDetailResponse> detail(@PathVariable String mapId) {
        MapConfig map = gameConfigService.getMapRequired(mapId);
        List<MonsterConfig> monsters = new ArrayList<MonsterConfig>();
        if (map.getMonsterIds() != null) {
            for (String monsterId : map.getMonsterIds()) {
                monsters.add(gameConfigService.getMonsterRequired(monsterId));
            }
        }
        return ApiResponse.ok(MapDetailResponse.from(map, monsters));
    }

    @GetMapping("/{mapId}/scene")
    public ApiResponse<MapSceneResponse> scene(@PathVariable String mapId) {
        return ApiResponse.ok(mapInteractionService.scene(AuthContext.getRequired(), mapId));
    }
}
