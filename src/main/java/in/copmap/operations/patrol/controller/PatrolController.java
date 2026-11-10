package in.copmap.operations.patrol.controller;

import in.copmap.auth.security.AuthUser;
import in.copmap.common.api.ApiResponse;
import in.copmap.common.api.PageResponse;
import in.copmap.common.audit.AuditLog;
import in.copmap.operations.patrol.domain.PatrolStatus;
import in.copmap.operations.patrol.dto.ClosePatrolRequest;
import in.copmap.operations.patrol.dto.CreatePatrolRequest;
import in.copmap.operations.patrol.dto.PatrolResponse;
import in.copmap.operations.patrol.service.PatrolService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/patrols")
@RequiredArgsConstructor
public class PatrolController {

    private final PatrolService patrolService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<PatrolResponse> create(
        @Valid @RequestBody CreatePatrolRequest req,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.create(req, me));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<PatrolResponse>> list(
        @RequestParam(required = false) PatrolStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.list(status, date, page, size, me));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PatrolResponse> getById(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.getById(id, me));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<PatrolResponse> publish(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.publish(id, me));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<PatrolResponse> close(
        @PathVariable Long id,
        @Valid @RequestBody ClosePatrolRequest req,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.close(id, req, me));
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<List<AuditLog>> getAudit(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(patrolService.getAuditTrail(id, me));
    }
}
