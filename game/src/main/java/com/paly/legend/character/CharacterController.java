package com.paly.legend.character;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import com.paly.legend.config.ClassConfig;
import com.paly.legend.config.GameConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final GameConfigService gameConfigService;

    public CharacterController(CharacterService characterService, GameConfigService gameConfigService) {
        this.characterService = characterService;
        this.gameConfigService = gameConfigService;
    }

    @PostMapping
    public ApiResponse<CreateCharacterResponse> create(@Valid @RequestBody CreateCharacterRequest request) {
        return ApiResponse.ok(characterService.create(AuthContext.getRequired(), request));
    }

    @GetMapping("/me")
    public ApiResponse<CharacterResponse> me() {
        return ApiResponse.ok(characterService.me(AuthContext.getRequired()));
    }

    @GetMapping("/classes")
    public ApiResponse<List<ClassResponse>> classes() {
        List<ClassResponse> responses = new ArrayList<ClassResponse>();
        for (ClassConfig config : gameConfigService.listClasses()) {
            responses.add(ClassResponse.from(config));
        }
        return ApiResponse.ok(responses);
    }
}
