package in.copmap.auth.controller;

import in.copmap.auth.dto.LoginRequest;
import in.copmap.auth.dto.LoginResponse;
import in.copmap.auth.dto.UserResponse;
import in.copmap.auth.security.AuthUser;
import in.copmap.auth.service.AuthService;
import in.copmap.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(auth.login(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String header) {
        auth.logout(header.substring(7));
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('OFFICER', 'PLANNER', 'SUPERVISOR')")
    public ApiResponse<AuthUser> me(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok(user);
    }

    @GetMapping("/rbac/probe")
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<String> rbacProbe() {
        return ApiResponse.ok("planner-or-supervisor access granted");
    }
}
