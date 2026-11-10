package in.copmap.common.audit;

import in.copmap.common.api.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasAnyRole('PLANNER', 'SUPERVISOR')")
    public ApiResponse<List<AuditLog>> list() {
        return ApiResponse.ok(repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
