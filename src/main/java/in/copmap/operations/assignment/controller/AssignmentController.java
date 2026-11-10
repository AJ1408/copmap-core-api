package in.copmap.operations.assignment.controller;

import in.copmap.auth.security.AuthUser;
import in.copmap.common.api.ApiResponse;
import in.copmap.operations.assignment.dto.AssignOfficersRequest;
import in.copmap.operations.assignment.dto.AssignmentResponse;
import in.copmap.operations.assignment.service.AssignmentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/patrols/{id}/assignments")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<List<AssignmentResponse>> assignOfficers(
        @PathVariable Long id,
        @Valid @RequestBody AssignOfficersRequest req,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(assignmentService.assignOfficers(id, req, me));
    }

    @GetMapping("/patrols/{id}/assignments")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<List<AssignmentResponse>> getAssignments(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(assignmentService.getAssignments(id, me));
    }

    @GetMapping("/me/assignments")
    @PreAuthorize("hasRole('OFFICER')")
    public ApiResponse<List<AssignmentResponse>> getMyAssignments(
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(assignmentService.getMyAssignments(me));
    }

    @PostMapping("/patrols/{id}/start")
    @PreAuthorize("hasRole('OFFICER')")
    public ApiResponse<AssignmentResponse> startDuty(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(assignmentService.startDuty(id, me));
    }

    @PostMapping("/patrols/{id}/end")
    @PreAuthorize("hasRole('OFFICER')")
    public ApiResponse<AssignmentResponse> endDuty(
        @PathVariable Long id,
        @AuthenticationPrincipal AuthUser me
    ) {
        return ApiResponse.ok(assignmentService.endDuty(id, me));
    }
}
