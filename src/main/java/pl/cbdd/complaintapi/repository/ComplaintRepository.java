package pl.cbdd.complaintapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.cbdd.complaintapi.model.Complaint;

import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
}
