package pl.cbdd.complaintapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.model.Complaint;
import pl.cbdd.complaintapi.repository.ComplaintRepository;
import pl.cbdd.complaintapi.service.GeoLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @MockBean
    private GeoLocationService geoLocationService;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        Mockito.when(geoLocationService.getCountryByIp(anyString())).thenReturn("Poland");
    }

    @Test
    void addComplaint_ShouldCreateAndReturnComplaint() throws Exception {

        ComplaintRequest complaintRequest = ComplaintRequest.builder()
                .productId("product-123")
                .content("This is a test complaint")
                .reporter("John Doe")
                .build();

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
                .andExpect(jsonPath("$.country").value("Poland"));
    }

    @Test
    void getComplaint_ShouldReturnComplaint() throws Exception {

        Complaint complaint = Complaint.builder()
                .productId("product-123")
                .content("Test complaint content")
                .reporter("Jane Doe")
                .country("Germany")
                .build();

        var savedComplaint = complaintRepository.save(complaint);

        mockMvc.perform(get("/api/v1/complaints/{id}", savedComplaint.getId().toString())
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

        Complaint complaint = Complaint.builder()
                .productId("product-123")
                .content("Original content")
                .reporter("John Doe")
                .country("France")
                .build();

        var savedComplaint = complaintRepository.save(complaint);

        ComplaintRequest updateRequest = ComplaintRequest.builder()
                .content("Updated content")
                .build();
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/v1/complaints/{id}", savedComplaint.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedComplaint.getId().toString()))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    void getAllComplaints_ShouldReturnListOfComplaints() throws Exception {
        for (int i = 1; i <= 3; i++) {
            Complaint complaint = Complaint.builder()
                    .productId("product-" + i)
                    .content("Complaint content " + i)
                    .reporter("Reporter " + i)
                    .country("Country " + i)
                    .build();
            complaintRepository.save(complaint);
        }

        mockMvc.perform(get("/api/v1/complaints/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}