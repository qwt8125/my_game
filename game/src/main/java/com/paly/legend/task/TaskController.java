package com.paly.legend.task;

import java.util.List;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> list() {
        return ApiResponse.ok(taskService.list(AuthContext.getRequired()));
    }

    @PostMapping("/{taskId}/claim")
    public ApiResponse<TaskClaimResponse> claim(@PathVariable String taskId) {
        return ApiResponse.ok(taskService.claim(AuthContext.getRequired(), taskId));
    }
}
