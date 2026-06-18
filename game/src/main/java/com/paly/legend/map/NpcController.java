package com.paly.legend.map;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/npcs")
public class NpcController {

    private final MapInteractionService mapInteractionService;

    public NpcController(MapInteractionService mapInteractionService) {
        this.mapInteractionService = mapInteractionService;
    }

    @PostMapping("/{npcId}/talk")
    public ApiResponse<NpcTalkResponse> talk(@PathVariable String npcId) {
        return ApiResponse.ok(mapInteractionService.talk(AuthContext.getRequired(), npcId));
    }
}
