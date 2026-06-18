package com.paly.legend.map;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map-events")
public class MapEventController {

    private final MapInteractionService mapInteractionService;

    public MapEventController(MapInteractionService mapInteractionService) {
        this.mapInteractionService = mapInteractionService;
    }

    @PostMapping("/{eventId}/trigger")
    public ApiResponse<MapEventTriggerResponse> trigger(@PathVariable String eventId) {
        return ApiResponse.ok(mapInteractionService.trigger(AuthContext.getRequired(), eventId));
    }
}
