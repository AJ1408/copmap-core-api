package in.copmap.auth.dto;

import in.copmap.auth.domain.Role;
import in.copmap.auth.domain.User;

public record UserResponse(Long id, String badgeNo, String fullName, String rank, Role role, Long stationId) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getBadgeNo(), user.getFullName(), user.getRank(),
                user.getRole(), user.getStationId());
    }
}
