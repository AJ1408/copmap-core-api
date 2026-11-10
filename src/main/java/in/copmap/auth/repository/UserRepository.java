package in.copmap.auth.repository;

import in.copmap.auth.domain.Role;
import in.copmap.auth.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByBadgeNo(String badgeNo);
    List<User> findByStationIdAndRoleAndActiveTrue(Long stationId, Role role);
}
