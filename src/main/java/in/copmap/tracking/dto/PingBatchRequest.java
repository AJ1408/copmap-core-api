package in.copmap.tracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PingBatchRequest(
        @NotNull Long patrolId,
        @NotEmpty @Size(max = 100) List<@Valid PingDto> pings
) {}
