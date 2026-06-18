package com.paly.legend.battle;

import java.io.IOException;
import java.io.OutputStream;

import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import com.paly.legend.common.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;
    private final ObjectMapper objectMapper;

    public BattleController(BattleService battleService, ObjectMapper objectMapper) {
        this.battleService = battleService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/fight")
    public ApiResponse<BattleResponse> fight(@Valid @RequestBody BattleRequest request) {
        return ApiResponse.ok(battleService.fight(AuthContext.getRequired(), request));
    }

    @PostMapping("/start")
    public ApiResponse<BattleSessionResponse> start(@Valid @RequestBody BattleRequest request) {
        return ApiResponse.ok(battleService.start(AuthContext.getRequired(), request));
    }

    @PostMapping("/encounter/start")
    public ApiResponse<BattleSessionResponse> startEncounter(@Valid @RequestBody BattleEncounterRequest request) {
        return ApiResponse.ok(battleService.startEncounter(AuthContext.getRequired(), request));
    }

    @PostMapping("/{battleId}/next")
    public ApiResponse<BattleSessionResponse> next(@PathVariable long battleId,
                                                   @RequestBody(required = false) BattleActionRequest request) {
        return ApiResponse.ok(battleService.next(AuthContext.getRequired(), battleId, request));
    }

    @PostMapping("/{battleId}/skill")
    public ApiResponse<BattleSessionResponse> castSkill(@PathVariable long battleId,
                                                        @Valid @RequestBody BattleSkillRequest request) {
        return ApiResponse.ok(battleService.castSkill(AuthContext.getRequired(), battleId, request));
    }

    @GetMapping("/{battleId}")
    public ApiResponse<BattleSessionResponse> status(@PathVariable long battleId) {
        return ApiResponse.ok(battleService.status(AuthContext.getRequired(), battleId));
    }

    @GetMapping(value = "/{battleId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamingResponseBody stream(@PathVariable long battleId) {
        CurrentUser currentUser = AuthContext.getRequired();
        BattleSessionResponse initial = battleService.status(currentUser, battleId);
        return outputStream -> streamBattle(currentUser, battleId, initial, outputStream);
    }

    private void streamBattle(CurrentUser currentUser, long battleId, BattleSessionResponse initial,
                              OutputStream outputStream) throws IOException {
        BattleSessionResponse current = initial;
        sendEvent(outputStream, "state", current);

        while ("running".equals(current.getStatus())) {
            sleepQuietly(Math.max(700, current.getSuggestedDelayMs()));
            current = battleService.next(currentUser, battleId);
            sendEvent(outputStream, "finished".equals(current.getStatus()) ? "finished" : "action", current);
        }
    }

    private void sendEvent(OutputStream outputStream, String event, BattleSessionResponse response) throws IOException {
        outputStream.write(("event: " + event + "\n").getBytes("UTF-8"));
        outputStream.write(("data: " + objectMapper.writeValueAsString(response) + "\n\n").getBytes("UTF-8"));
        outputStream.flush();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
