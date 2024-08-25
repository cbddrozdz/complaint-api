package pl.cbdd.complaintapi.service;

import org.springframework.stereotype.Service;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;

import java.util.List;
import java.util.UUID;

@Service
public interface ComplaintService {
    ComplaintResponse addComplaint(ComplaintRequest complaintRequest);

    ComplaintResponse getComplaint(UUID id);

    List<ComplaintResponse> getAllComplaints(int page, int size);

    ComplaintResponse updateComplaint(UUID id, UpdateComplaintRequest updateComplaintRequest);

}
