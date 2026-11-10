package in.copmap.operations.bandobast.repository;

import in.copmap.operations.bandobast.domain.BandobastOperation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BandobastOperationRepository extends JpaRepository<BandobastOperation, Long> {
    List<BandobastOperation> findByStationId(Long stationId);
}

