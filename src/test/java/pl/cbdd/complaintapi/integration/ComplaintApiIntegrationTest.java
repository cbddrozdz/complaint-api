package pl.cbdd.complaintapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class ComplaintApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private GeoLocationService geoLocationService;

    @Mock
    private ComplaintRequest complaintRequest;

    @Mock
    private UpdateComplaintRequest updateComplaintRequest;

    @BeforeEach
    void setUp() {
        when(geoLocationService.getCountryByIp("123.123.123.123")).thenReturn("Poland");
    }

    @Test
    void addComplaint_ShouldCreateAndReturnComplaint() throws Exception {
        when(geoLocationService.getCountryByIp("123.123.123.123")).thenReturn("Poland");

        when(complaintRequest.getProductId()).thenReturn("product-123");
        when(complaintRequest.getReporter()).thenReturn("John Doe");
        when(complaintRequest.getContent()).thenReturn("This is a test complaint");
        when(complaintRequest.getCountry()).thenReturn("Poland");

        String complaintRequestJson = objectMapper.writeValueAsString(complaintRequest);

        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "123.123.123.123")
                        .content(complaintRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.productId").value("product-123"))
                .andExpect(jsonPath("$.content").value("This is a test complaint"))
                .andExpect(jsonPath("$.reporter").value("John Doe"))
                .andExpect(jsonPath("$.country").value("China"));
    }

    @Test
    void getComplaint_ShouldReturnComplaint() throws Exception {
        Complaint savedComplaint = new Complaint();
        savedComplaint.setProductId("product-123");
        savedComplaint.setReporter("Jane Doe");
        savedComplaint.setContent("Test complaint content");
        savedComplaint.setCountry("Germany");
        savedComplaint = complaintRepository.save(savedComplaint);

        mockMvc.perform(get("/api/v1/complaints/{id}", savedComplaint.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedComplaint.getId().toString()))
                .andExpect(jsonPath("$.productId").value("product-123"))
                .andExpect(jsonPath("$.content").value("Test complaint content"))
                .andExpect(jsonPath("$.reporter").value("Jane Doe"))
                .andExpect(jsonPath("$.country").value("Germany"));
    }

    @Test
    void updateComplaint_ShouldUpdateAndReturnComplaint() throws Exception {
        Complaint savedComplaint = new Complaint();
        savedComplaint.setProductId("product-123");
        savedComplaint.setReporter("John Doe");
        savedComplaint.setContent("Original content");
        savedComplaint.setCountry("France");
        savedComplaint = complaintRepository.save(savedComplaint);

        when(updateComplaintRequest.getId()).thenReturn(savedComplaint.getId().toString());
        when(updateComplaintRequest.getContent()).thenReturn("Updated content");

        String updateRequestJson = objectMapper.writeValueAsString(updateComplaintRequest);

        mockMvc.perform(put("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedComplaint.getId().toString()))
                .andExpect(jsonPath("$.content").value("Updated content"));

        Complaint updatedComplaint = complaintRepository.findById(savedComplaint.getId()).orElseThrow();
        assertThat(updatedComplaint.getContent()).isEqualTo("Updated content");
    }

    @Test
    void getAllComplaints_ShouldReturnListOfComplaintsInDescendingOrder() throws Exception {

        complaintRepository.deleteAll();

        Complaint savedComplaint1 = new Complaint();
        savedComplaint1.setProductId("product-1");
        savedComplaint1.setReporter("Reporter 1");
        savedComplaint1.setContent("Complaint content 1");
        savedComplaint1.setCountry("Country 1");
        savedComplaint1.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(10)));
        Complaint complaint1 = complaintRepository.save(savedComplaint1);

        Complaint savedComplaint2 = new Complaint();
        savedComplaint2.setProductId("product-2");
        savedComplaint2.setReporter("Reporter 2");
        savedComplaint2.setContent("Complaint content 2");
        savedComplaint2.setCountry("Country 2");
        savedComplaint2.setCreatedAt(Timestamp.from(Instant.now())); // Ustawienie nowszej daty
        Complaint complaint2 = complaintRepository.save(savedComplaint2);

        mockMvc.perform(get("/api/v1/complaints/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(complaint2.getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(complaint1.getId().toString()));
    }
}