package pl.cbdd.complaintapi.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.exception.ComplaintCreationException;
import pl.cbdd.complaintapi.exception.ComplaintNotFoundException;
import pl.cbdd.complaintapi.exception.ComplaintUpdateException;
import pl.cbdd.complaintapi.model.Complaint;
import pl.cbdd.complaintapi.repository.ComplaintRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ModelMapper modelMapper;

    @Override
    public ComplaintResponse addComplaint(ComplaintRequest complaintRequest) {
        try {
            Optional<Complaint> existingComplaintOpt = complaintRepository
                    .findByProductIdAndReporter(complaintRequest.getProductId(), complaintRequest.getReporter());

            Complaint complaint;
            if (existingComplaintOpt.isPresent()) {
                complaint = complaintRepository.findAndLockById(existingComplaintOpt.get().getId());
                complaint.setReportCount(complaint.getReportCount() + 1);
                complaintRepository.save(complaint);
            } else {
                complaint = new Complaint();
                complaint.setProductId(complaintRequest.getProductId());
                complaint.setContent(complaintRequest.getContent());
                complaint.setCreatedAt(Timestamp.from(Instant.now()));
                complaint.setReporter(complaintRequest.getReporter());
                complaint.setCountry(complaintRequest.getCountry());
                complaint.setReportCount(1);
                complaintRepository.save(complaint);
            }
            return modelMapper.map(complaint, ComplaintResponse.class);
        } catch (Exception e) {
            throw new ComplaintCreationException("Failed to add complaint: " + e.getMessage(), e);
        }
    }

    @Override
    public ComplaintResponse getComplaint(UUID id) {
        try {
            Complaint complaint = complaintRepository.findById(id)
                    .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + id));
            return modelMapper.map(complaint, ComplaintResponse.class);
        } catch (ComplaintNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get complaint: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ComplaintResponse> getAllComplaints(int page, int size) {
        try {
            return complaintRepository.findAll(PageRequest.of(page, size)).stream()
                    .map(complaint -> modelMapper.map(complaint, ComplaintResponse.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all complaints: " + e.getMessage(), e);
        }
    }

    @Override
    public ComplaintResponse updateComplaint(UUID id, UpdateComplaintRequest updateComplaintRequest) {
        try {
            Complaint complaint = complaintRepository.findAndLockById(id);
            if (complaint == null) {
                throw new ComplaintNotFoundException("Complaint not found with id: " + id);
            }

            complaint.setContent(updateComplaintRequest.getContent());
            complaintRepository.save(complaint);

            return modelMapper.map(complaint, ComplaintResponse.class);
        } catch (ComplaintNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ComplaintUpdateException("Failed to update complaint: " + e.getMessage(), e);
        }
    }
}