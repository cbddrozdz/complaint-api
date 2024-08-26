package pl.cbdd.complaintapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.model.Complaint;
import pl.cbdd.complaintapi.repository.ComplaintRepository;
import pl.cbdd.complaintapi.service.GeoLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class ComplaintApiIntegrationTest {

    @Mock
    private ComplaintRequest complaintRequest;

    @Mock
    private UpdateComplaintRequest updateComplaintRequest;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintRepository complaintRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeoLocationService geoLocationService;

    @BeforeEach
    void setUp() {
        when(geoLocationService.getCountryByIp(anyString())).thenReturn("Poland");
    }

    @Test
    void addComplaint_ShouldCreateAndReturnComplaint() throws Exception {
        doReturn("product-123").when(complaintRequest).getProductId();
        doReturn("John Doe").when(complaintRequest).getReporter();
        doReturn("This is a test complaint").when(complaintRequest).getContent();
        doReturn("Poland").when(complaintRequest).getCountry();

        doAnswer(invocation -> {
            Complaint savedComplaint = invocation.getArgument(0);
            savedComplaint.setId(UUID.randomUUID());
            return savedComplaint;
        }).when(complaintRepository).save(any(Complaint.class));

        String complaintRequestJson = objectMapper.writeValueAsString(complaintRequest);

        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "123.123.123.123")
                        .content(complaintRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists()) // Sprawdzenie, że ID istnieje
                .andExpect(jsonPath("$.productId").value("product-123"))
                .andExpect(jsonPath("$.content").value("This is a test complaint"))
                .andExpect(jsonPath("$.reporter").value("John Doe"))
                .andExpect(jsonPath("$.country").value("Poland"));
    }

    @Test
    void getComplaint_ShouldReturnComplaint() throws Exception {
        UUID complaintId = UUID.randomUUID();
        Complaint mockComplaint = Mockito.mock(Complaint.class);

        doReturn(complaintId).when(mockComplaint).getId();
        doReturn("product-123").when(mockComplaint).getProductId();
        doReturn("Test complaint content").when(mockComplaint).getContent();
        doReturn("Jane Doe").when(mockComplaint).getReporter();
        doReturn("Germany").when(mockComplaint).getCountry();

        doReturn(Optional.of(mockComplaint)).when(complaintRepository).findById(complaintId);

        mockMvc.perform(get("/api/v1/complaints/{id}", complaintId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(complaintId.toString()))
                .andExpect(jsonPath("$.productId").value("product-123"))
                .andExpect(jsonPath("$.content").value("Test complaint content"))
                .andExpect(jsonPath("$.reporter").value("Jane Doe"))
                .andExpect(jsonPath("$.country").value("Germany"));
    }

    @Test
    void updateComplaint_ShouldUpdateAndReturnComplaint() throws Exception {
        UUID complaintId = UUID.randomUUID();

        doReturn(complaintId.toString()).when(updateComplaintRequest).getId();
        doReturn("Updated content").when(updateComplaintRequest).getContent();

        Complaint mockComplaint = Mockito.mock(Complaint.class);

        doReturn(Optional.of(mockComplaint)).when(complaintRepository).findById(complaintId);

        doReturn(complaintId).when(mockComplaint).getId();
        doReturn("product-123").when(mockComplaint).getProductId();
        doReturn("Original content").when(mockComplaint).getContent();
        doReturn("John Doe").when(mockComplaint).getReporter();
        doReturn("France").when(mockComplaint).getCountry();

        doAnswer(invocation -> {
            when(mockComplaint.getContent()).thenReturn("Updated content");
            return mockComplaint;
        }).when(complaintRepository).save(mockComplaint);

        String updateRequestJson = objectMapper.writeValueAsString(updateComplaintRequest);

        mockMvc.perform(put("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(complaintId.toString()))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void getAllComplaints_ShouldReturnListOfComplaints() throws Exception {
        Complaint mockComplaint1 = Mockito.mock(Complaint.class);
        Complaint mockComplaint2 = Mockito.mock(Complaint.class);

        doReturn(UUID.randomUUID()).when(mockComplaint1).getId();
        doReturn("product-1").when(mockComplaint1).getProductId();
        doReturn("Complaint content 1").when(mockComplaint1).getContent();
        doReturn("Reporter 1").when(mockComplaint1).getReporter();
        doReturn("Country 1").when(mockComplaint1).getCountry();

        doReturn(UUID.randomUUID()).when(mockComplaint2).getId();
        doReturn("product-2").when(mockComplaint2).getProductId();
        doReturn("Complaint content 2").when(mockComplaint2).getContent();
        doReturn("Reporter 2").when(mockComplaint2).getReporter();
        doReturn("Country 2").when(mockComplaint2).getCountry();

        Page<Complaint> page = new PageImpl<>(List.of(mockComplaint1, mockComplaint2));
        doReturn(page).when(complaintRepository).findAll(any(Pageable.class));

        mockMvc.perform(get("/api/v1/complaints/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[1].id").exists());
    }
}