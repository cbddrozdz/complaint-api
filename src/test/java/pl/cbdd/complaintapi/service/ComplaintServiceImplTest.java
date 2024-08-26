package pl.cbdd.complaintapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.exception.ComplaintCreationException;
import pl.cbdd.complaintapi.exception.ComplaintNotFoundException;
import pl.cbdd.complaintapi.model.Complaint;
import pl.cbdd.complaintapi.repository.ComplaintRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private Complaint complaint;
    private ComplaintRequest complaintRequest;
    private UpdateComplaintRequest updateComplaintRequest;
    private UUID complaintId;

    @BeforeEach
    void setUp() {
        complaintId = UUID.randomUUID();
        complaint = new Complaint();
        complaint.setId(complaintId);
        complaint.setProductId("prod1");
        complaint.setContent("Initial content");
        complaint.setCreatedAt(Timestamp.from(Instant.now()));
        complaint.setReporter("reporter@example.com");
        complaint.setCountry("Poland");
        complaint.setReportCount(1);

        complaintRequest = new ComplaintRequest();
        complaintRequest.setProductId("prod1");
        complaintRequest.setContent("New complaint content");
        complaintRequest.setReporter("reporter@example.com");
        complaintRequest.setCountry("Poland");

        updateComplaintRequest = new UpdateComplaintRequest();
        updateComplaintRequest.setId(complaintId.toString());
        updateComplaintRequest.setContent("Updated content");
    }

    @Test
    void shouldAddNewComplaintWhenNoExistingComplaint() {
        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint savedComplaint = invocation.getArgument(0);
            savedComplaint.setId(complaintId);
            return savedComplaint;
        });
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        ComplaintResponse response = complaintService.addComplaint(complaintRequest);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> verify(complaintRepository).save(any(Complaint.class))
        );
    }

    @Test
    void shouldIncrementReportCountWhenComplaintExists() {
        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.of(complaint));
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        ComplaintResponse response = complaintService.addComplaint(complaintRequest);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(complaint.getReportCount()).isEqualTo(2),
                () -> verify(complaintRepository).save(complaint)
        );
    }

    @Test
    void shouldThrowExceptionWhenSavingComplaintFails() {
        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenThrow(new RuntimeException("Database error"));

        ComplaintCreationException exception = assertThrows(ComplaintCreationException.class, () -> complaintService.addComplaint(complaintRequest));

        assertAll(
                () -> assertThat(exception.getMessage()).contains("Failed to add complaint"),
                () -> verify(complaintRepository).save(any(Complaint.class))
        );
    }

    @Test
    void shouldNotCreateNewComplaintWhenDuplicateExists() {
        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.of(complaint));
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        ComplaintResponse response = complaintService.addComplaint(complaintRequest);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(complaint.getReportCount()).isEqualTo(2),
                () -> verify(complaintRepository).save(complaint)
        );
    }

    @Test
    void shouldThrowExceptionWhenComplaintNotFound() {
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ComplaintNotFoundException.class, () -> complaintService.updateComplaint(updateComplaintRequest));
    }

    @Test
    void shouldUpdateComplaintContent() {
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        ComplaintResponse response = complaintService.updateComplaint(updateComplaintRequest);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(complaint.getContent()).isEqualTo("Updated content"),
                () -> verify(complaintRepository).save(complaint)
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingComplaintFails() {
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> complaintService.updateComplaint(updateComplaintRequest));

        assertAll(
                () -> assertThat(exception.getMessage()).contains("Database error"),
                () -> verify(complaintRepository).save(complaint)
        );
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentComplaint() {
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ComplaintNotFoundException.class, () -> complaintService.getComplaint(complaintId));
    }

    @Test
    void shouldReturnEmptyPageWhenNoComplaintsExist() {
        when(complaintRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<ComplaintResponse> response = complaintService.getAllComplaints(Pageable.unpaged());

        assertAll(
                () -> assertThat(response.getTotalElements()).isEqualTo(0),
                () -> verify(complaintRepository).findAll(any(Pageable.class))
        );
    }

    @Test
    void shouldReturnAllComplaintsWhenTheyExist() {
        when(complaintRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(complaint)));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        Page<ComplaintResponse> response = complaintService.getAllComplaints(Pageable.unpaged());

        assertAll(
                () -> assertThat(response.getTotalElements()).isEqualTo(1),
                () -> verify(complaintRepository).findAll(any(Pageable.class))
        );
    }

    @Test
    void shouldNotUpdateComplaintWhenContentIsNull() {
        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(new ComplaintResponse());

        updateComplaintRequest.setContent(null);

        ComplaintResponse response = complaintService.updateComplaint(updateComplaintRequest);

        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(complaint.getContent()).isEqualTo("Initial content"),
                () -> verify(complaintRepository, never()).save(complaint)
        );
    }
}