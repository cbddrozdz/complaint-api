package pl.cbdd.complaintapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.service.ComplaintService;
import pl.cbdd.complaintapi.service.GeoLocationService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComplaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComplaintService complaintService;

    @Mock
    private GeoLocationService geoLocationService;

    @InjectMocks
    private ComplaintController complaintController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(complaintController).build();
    }

    @Test
    void addComplaint_ShouldReturnComplaintResponse() throws Exception {

        ComplaintRequest complaintRequest = new ComplaintRequest();
        complaintRequest.setCountry("Poland");

        ComplaintResponse complaintResponse = new ComplaintResponse();
        complaintResponse.setId(UUID.randomUUID());
        complaintResponse.setCountry("Poland");

        when(geoLocationService.getCountryByIp(anyString())).thenReturn("Poland");
        when(complaintService.addComplaint(any(ComplaintRequest.class))).thenReturn(complaintResponse);

        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"field1\":\"value1\",\"field2\":\"value2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.country").value("Poland"));
    }

    @Test
    void getComplaint_ShouldReturnComplaintResponse() throws Exception {

        UUID id = UUID.randomUUID();
        ComplaintResponse complaintResponse = new ComplaintResponse();
        complaintResponse.setId(id);
        complaintResponse.setCountry("Poland");

        when(complaintService.getComplaint(id)).thenReturn(complaintResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/complaints/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.country").value("Poland"));
    }

    @Test
    void getAllComplaints_ShouldReturnListOfComplaints() throws Exception {

        ComplaintResponse complaintResponse = new ComplaintResponse();
        complaintResponse.setId(UUID.randomUUID());
        complaintResponse.setCountry("Poland");

        List<ComplaintResponse> complaints = Collections.singletonList(complaintResponse);

        when(complaintService.getAllComplaints(0, 10)).thenReturn(complaints);

        mockMvc.perform(get("/api/v1/complaints/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].country").value("Poland"));
    }

    @Test
    void updateComplaint_ShouldReturnUpdatedComplaintResponse() throws Exception {

        UUID id = UUID.randomUUID();
        UpdateComplaintRequest updateComplaintRequest = new UpdateComplaintRequest();
        updateComplaintRequest.setContent("new content");

        ComplaintResponse complaintResponse = new ComplaintResponse();
        complaintResponse.setId(id);
        complaintResponse.setContent("new content");
        complaintResponse.setCountry("Poland");

        when(complaintService.updateComplaint(eq(id), any(UpdateComplaintRequest.class))).thenReturn(complaintResponse);

        mockMvc.perform(put("/api/v1/complaints/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"new content\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.content").value("new content"))
                .andExpect(jsonPath("$.country").value("Poland"));
    }
}