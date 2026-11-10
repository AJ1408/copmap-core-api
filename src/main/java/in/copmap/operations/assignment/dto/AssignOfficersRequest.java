package in.copmap.operations.assignment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AssignOfficersRequest(
    @NotEmpty
    List<@NotNull Long> officerIds
) {}
