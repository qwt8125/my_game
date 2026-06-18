package com.paly.legend.equipment;

import javax.validation.Valid;

import com.paly.legend.common.ApiResponse;
import com.paly.legend.common.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ApiResponse<EquipmentResponse> list() {
        return ApiResponse.ok(equipmentService.list(AuthContext.getRequired()));
    }

    @PostMapping("/equip")
    public ApiResponse<EquipmentResponse> equip(@Valid @RequestBody EquipItemRequest request) {
        return ApiResponse.ok(equipmentService.equip(AuthContext.getRequired(), request));
    }

    @PostMapping("/unequip")
    public ApiResponse<EquipmentResponse> unequip(@Valid @RequestBody UnequipItemRequest request) {
        return ApiResponse.ok(equipmentService.unequip(AuthContext.getRequired(), request));
    }

    @PostMapping("/enhance")
    public ApiResponse<EnhanceItemResponse> enhance(@Valid @RequestBody EnhanceItemRequest request) {
        return ApiResponse.ok(equipmentService.enhance(AuthContext.getRequired(), request));
    }

    @PostMapping("/reroll-affixes")
    public ApiResponse<RerollAffixResponse> rerollAffixes(@Valid @RequestBody RerollAffixRequest request) {
        return ApiResponse.ok(equipmentService.rerollAffixes(AuthContext.getRequired(), request));
    }

    @PostMapping("/decompose")
    public ApiResponse<DecomposeItemResponse> decompose(@Valid @RequestBody DecomposeItemRequest request) {
        return ApiResponse.ok(equipmentService.decompose(AuthContext.getRequired(), request));
    }
}
