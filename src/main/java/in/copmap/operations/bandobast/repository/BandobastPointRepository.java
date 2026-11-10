package in.copmap.operations.bandobast.repository;

import in.copmap.operations.bandobast.domain.BandobastPoint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BandobastPointRepository extends JpaRepository<BandobastPoint, Long> {
    List<BandobastPoint> findByOperationId(Long operationId);
}

