package pl.cbdd.complaintapi.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pl.cbdd.complaintapi.model.Complaint;

import java.util.Optional;
import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select c from Complaint c where c.id = :id")
    Complaint findAndLockById(UUID id);

    Optional<Complaint> findByProductIdAndReporter(String productId, String reporter);
}
