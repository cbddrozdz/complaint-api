package pl.cbdd.complaintapi.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.exception.ComplaintCreationException;
import pl.cbdd.complaintapi.exception.ComplaintNotFoundException;
import pl.cbdd.complaintapi.model.Complaint;
import pl.cbdd.complaintapi.repository.ComplaintRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Transactional
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ComplaintResponse addComplaint(ComplaintRequest complaintRequest) {
        try {
            Complaint complaint = new Complaint();
            AtomicReference<ComplaintResponse> complaintResponseAtomic = new AtomicReference<>();
            complaintRepository
                    .findByProductIdAndReporter(complaintRequest.getProductId(), complaintRequest.getReporter())
                    .ifPresentOrElse(comp -> {
                                complaintRepository.findById(comp.getId())
                                        .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + comp.getId()));

                                comp.setReportCount(comp.getReportCount() + 1);
                                complaintRepository.save(comp);
                                complaintResponseAtomic.set(modelMapper.map(comp, ComplaintResponse.class));
                            }, () -> {
                                complaint.setProductId(complaintRequest.getProductId());
                                complaint.setContent(complaintRequest.getContent());
                                complaint.setCreatedAt(Timestamp.from(Instant.now()));
                                complaint.setReporter(complaintRequest.getReporter());
                                complaint.setCountry(complaintRequest.getCountry());
                                complaint.setReportCount(1);
                                complaintRepository.save(complaint);
                                complaintResponseAtomic.set(modelMapper.map(complaint, ComplaintResponse.class));
                            }
                    );

            return complaintResponseAtomic.get();
        } catch (Exception e) {
            throw new ComplaintCreationException("Failed to add complaint: " + e.getMessage(), e);
        }
    }

    @Override
    public ComplaintResponse getComplaint(UUID id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + id));
        return modelMapper.map(complaint, ComplaintResponse.class);
    }

    @Override
    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        return complaintRepository.findAll(pageable)
                .map(complaint -> modelMapper.map(complaint, ComplaintResponse.class));
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(UpdateComplaintRequest updateComplaintRequest) {
        Complaint complaint = complaintRepository.findById(UUID.fromString(updateComplaintRequest.getId()))
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found with id: " + updateComplaintRequest.getId()));

        if (updateComplaintRequest.getContent() != null) {
            complaint.setContent(updateComplaintRequest.getContent());
            complaintRepository.save(complaint);
        }

        return modelMapper.map(complaint, ComplaintResponse.class);
    }
}