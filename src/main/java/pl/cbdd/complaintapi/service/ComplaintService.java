package pl.cbdd.complaintapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;

import java.util.UUID;

@Service
public interface ComplaintService {
    ComplaintResponse addComplaint(ComplaintRequest complaintRequest);

    ComplaintResponse getComplaint(UUID id);

    Page<ComplaintResponse> getAllComplaints(Pageable pageable);

    ComplaintResponse updateComplaint(UpdateComplaintRequest updateComplaintRequest);

}
