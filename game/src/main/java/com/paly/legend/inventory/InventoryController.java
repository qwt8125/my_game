package com.paly.legend.inventory;

import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<InventoryListResponse> list() {
        return ApiResponse.ok(inventoryService.list(AuthContext.getRequired()));
    }

    @PostMapping("/materials/sell")
    public ApiResponse<SellItemResponse> sellMaterials(@Valid @RequestBody SellMaterialsRequest request) {
        return ApiResponse.ok(inventoryService.sellMaterials(AuthContext.getRequired(), request));
    }

    @PostMapping("/{inventoryItemId}/sell")
    public ApiResponse<SellItemResponse> sell(@PathVariable long inventoryItemId,
                                              @Valid @RequestBody SellItemRequest request) {
        return ApiResponse.ok(inventoryService.sell(AuthContext.getRequired(), inventoryItemId, request));
    }

    @PostMapping("/{inventoryItemId}/use")
    public ApiResponse<UseItemResponse> use(@PathVariable long inventoryItemId) {
        return ApiResponse.ok(inventoryService.use(AuthContext.getRequired(), inventoryItemId));
    }

    @PostMapping("/{inventoryItemId}/discard")
    public ApiResponse<DiscardItemResponse> discard(@PathVariable long inventoryItemId,
                                                    @Valid @RequestBody DiscardItemRequest request) {
        return ApiResponse.ok(inventoryService.discard(AuthContext.getRequired(), inventoryItemId, request));
    }
}
