package in.copmap.tracking.controller;

import in.copmap.auth.security.AuthUser;
import in.copmap.common.api.ApiResponse;
import in.copmap.tracking.dto.LivePositionResponse;
import in.copmap.tracking.dto.PingBatchRequest;
import in.copmap.tracking.service.TrackingService;
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
public class TrackingController {

    private final TrackingService trackingService;

    @PreAuthorize("hasRole('OFFICER')")
    @PostMapping("/locations")
    public ApiResponse<Void> ingest(
            @Valid @RequestBody PingBatchRequest req,
            @AuthenticationPrincipal AuthUser user) {
        trackingService.ingest(req, user);
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'PLANNER')")
    @GetMapping("/patrols/{id}/live")
    public ApiResponse<List<LivePositionResponse>> getLive(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(trackingService.getLivePositions(id, user));
    }
}
