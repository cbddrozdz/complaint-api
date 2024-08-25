package pl.cbdd.complaintapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplaintServiceImplTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private Complaint complaint;
    private ComplaintRequest complaintRequest;
    private ComplaintResponse complaintResponse;
    private UpdateComplaintRequest updateComplaintRequest;
    private UUID complaintId;

    @BeforeEach
    void setUp() {
        complaintId = UUID.randomUUID();

        complaint = Complaint.builder()
                .id(complaintId)
                .productId("prod1")
                .content("Initial content")
                .createdAt(Timestamp.from(Instant.now()))
                .reporter("reporter@example.com")
                .country("Poland")
                .reportCount(1)
                .build();

        complaintRequest = createComplaintRequest();
        complaintResponse = createComplaintResponse();
        updateComplaintRequest = createUpdateComplaintRequest();
    }

    private ComplaintRequest createComplaintRequest() {
        return ComplaintRequest.builder()
                .productId("prod1")
                .content("Test content")
                .reporter("reporter@example.com")
                .country("Poland")
                .build();
    }

    private ComplaintResponse createComplaintResponse() {
        return ComplaintResponse.builder()
                .id(complaintId)
                .productId("prod1")
                .content("Test content")
                .createdAt(Timestamp.from(Instant.now()))
                .reporter("reporter@example.com")
                .country("Poland")
                .reportCount(1)
                .build();
    }

    private UpdateComplaintRequest createUpdateComplaintRequest() {
        return UpdateComplaintRequest.builder()
                .content("Updated content")
                .build();
    }

    @Test
    void shouldAddNewComplaintWhenNoExistingComplaint() {

        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenReturn(complaint);
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(complaintResponse);

        ComplaintResponse response = complaintService.addComplaint(complaintRequest);

        verify(complaintRepository, times(1)).save(any(Complaint.class));
        assertEquals(complaintResponse.getProductId(), response.getProductId());
        assertEquals(1, response.getReportCount());
    }

    @Test
    void shouldIncrementReportCountWhenComplaintExists() {

        complaint.setReportCount(1);
        when(complaintRepository.findByProductIdAndReporter(anyString(), anyString())).thenReturn(Optional.of(complaint));
        when(complaintRepository.findAndLockById(any(UUID.class))).thenReturn(complaint);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint savedComplaint = invocation.getArgument(0);
            savedComplaint.setId(UUID.randomUUID());
            return savedComplaint;
        });
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenAnswer(invocation -> {
            Complaint mappedComplaint = invocation.getArgument(0);
            return ComplaintResponse.builder()
                    .id(mappedComplaint.getId())
                    .productId(mappedComplaint.getProductId())
                    .content(mappedComplaint.getContent())
                    .createdAt(mappedComplaint.getCreatedAt())
                    .reporter(mappedComplaint.getReporter())
                    .country(mappedComplaint.getCountry())
                    .reportCount(mappedComplaint.getReportCount())
                    .build();
        });

        ComplaintResponse response = complaintService.addComplaint(complaintRequest);

        assertNotNull(response);
        assertEquals(2, response.getReportCount());

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getReportCount());
    }

    @Test
    void shouldThrowExceptionWhenComplaintCreationFails() {

        when(complaintRepository.save(any(Complaint.class))).thenThrow(RuntimeException.class);

        assertThrows(ComplaintCreationException.class, () -> complaintService.addComplaint(complaintRequest));
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    void shouldGetComplaintById() {

        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.of(complaint));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(complaintResponse);

        ComplaintResponse response = complaintService.getComplaint(complaintId);


        assertEquals(complaintResponse.getProductId(), response.getProductId());
        verify(complaintRepository, times(1)).findById(complaintId);
    }

    @Test
    void shouldThrowExceptionWhenComplaintNotFound() {

        when(complaintRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ComplaintNotFoundException.class, () -> complaintService.getComplaint(complaintId));
        verify(complaintRepository, times(1)).findById(complaintId);
    }

    @Test
    void shouldUpdateComplaintContent() {

        when(complaintRepository.findAndLockById(any(UUID.class))).thenReturn(complaint);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(Complaint.class), eq(ComplaintResponse.class))).thenReturn(complaintResponse);

        ComplaintResponse response = complaintService.updateComplaint(complaintId, updateComplaintRequest);

        assertEquals(updateComplaintRequest.getContent(), complaint.getContent());
        verify(complaintRepository, times(1)).save(complaint);
    }

    @Test
    void shouldThrowExceptionWhenUpdateFails() {

        when(complaintRepository.findAndLockById(any(UUID.class))).thenThrow(RuntimeException.class);

        assertThrows(ComplaintUpdateException.class, () -> complaintService.updateComplaint(complaintId, updateComplaintRequest));
        verify(complaintRepository, times(1)).findAndLockById(complaintId);
    }
}