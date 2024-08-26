package pl.cbdd.complaintapi.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import pl.cbdd.complaintapi.model.Complaint;

import java.util.Optional;
import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    @Override
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Complaint> findById(UUID id);

    Optional<Complaint> findByProductIdAndReporter(String productId, String reporter);
}
