package in.copmap.alert.controller;

import in.copmap.alert.domain.AlertStatus;
import in.copmap.alert.dto.AlertResponse;
import in.copmap.alert.dto.SosRequest;
import in.copmap.alert.service.AlertService;
import in.copmap.auth.security.AuthUser;
import in.copmap.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping("/sos")
    public ApiResponse<AlertResponse> raiseSos(
            @Valid @RequestBody SosRequest req,
            @AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(alertService.raiseSos(req, user.userId()));
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'PLANNER')")
    @GetMapping
    public ApiResponse<List<AlertResponse>> list(
            @RequestParam(name = "status", required = false) AlertStatus status,
            @RequestParam(name = "patrolId", required = false) Long patrolId) {
        return ApiResponse.ok(alertService.list(status, patrolId));
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'PLANNER')")
    @PostMapping("/{id}/ack")
    public ApiResponse<AlertResponse> acknowledge(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(alertService.acknowledge(id, user.userId()));
    }
}
