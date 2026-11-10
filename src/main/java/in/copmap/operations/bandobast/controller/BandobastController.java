package in.copmap.operations.bandobast.controller;

import in.copmap.common.api.ApiResponse;
import in.copmap.common.exception.ErrorCode;
import in.copmap.operations.bandobast.dto.CheckInRequest;
import in.copmap.operations.bandobast.dto.CreateBandobastRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bandobast")
@PreAuthorize("hasAnyRole('PLANNER','SUPERVISOR')")
public class BandobastController {

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody(required = false) CreateBandobastRequest request) {
        return notImplemented();
    }

    @PostMapping("/{id}/points/{pointId}/checkin")
    @PreAuthorize("hasRole('OFFICER')")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @PathVariable Long id,
            @PathVariable Long pointId,
            @Valid @RequestBody(required = false) CheckInRequest request) {
        return notImplemented();
    }

    private ResponseEntity<ApiResponse<Void>> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.fail(ErrorCode.NOT_IMPLEMENTED, "Bandobast is designed but not implemented"));
    }
}

