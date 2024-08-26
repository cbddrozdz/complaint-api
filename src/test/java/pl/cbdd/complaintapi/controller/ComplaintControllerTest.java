package pl.cbdd.complaintapi.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;
import pl.cbdd.complaintapi.service.ComplaintService;
import pl.cbdd.complaintapi.service.GeoLocationService;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplaintController.class)
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ComplaintResponse complaintResponse;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private GeoLocationService geoLocationService;

    @Test
    void addComplaint_ShouldReturnComplaintResponse() throws Exception {

        ComplaintRequest complaintRequest = Mockito.mock(ComplaintRequest.class);
        doReturn("Poland").when(complaintRequest).getCountry();

        ComplaintResponse complaintResponse = Mockito.mock(ComplaintResponse.class);
        doReturn(UUID.randomUUID()).when(complaintResponse).getId();
        doReturn("Poland").when(complaintResponse).getCountry();

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
        ComplaintResponse complaintResponse = Mockito.mock(ComplaintResponse.class);
        doReturn(id).when(complaintResponse).getId();
        doReturn("Poland").when(complaintResponse).getCountry();

        when(complaintService.getComplaint(id)).thenReturn(complaintResponse);

        mockMvc.perform(get("/api/v1/complaints/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.country").value("Poland"));
    }

    @Test
    void getAllComplaints_ShouldReturnListOfComplaints() throws Exception {

        ComplaintResponse complaintResponse = Mockito.mock(ComplaintResponse.class);
        doReturn(UUID.fromString("c732cd78-572c-4059-bd62-61b9f5ed9251")).when(complaintResponse).getId();
        doReturn("Poland").when(complaintResponse).getCountry();
        doReturn(1).when(complaintResponse).getReportCount();

        Page<ComplaintResponse> complaints = new PageImpl<>(Collections.singletonList(complaintResponse), PageRequest.of(0, 10), 1);

        when(complaintService.getAllComplaints(any(Pageable.class))).thenReturn(complaints);

        mockMvc.perform(get("/api/v1/complaints/all"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("{\"content\":[{\"id\":\"c732cd78-572c-4059-bd62-61b9f5ed9251\"," +
                        "\"productId\":null,\"content\":null,\"createdAt\":null,\"reporter\":null,\"country\":\"Poland\"," +
                        "\"reportCount\":1}],\"pageable\":{\"pageNumber\":0,\"pageSize\":10,\"sort\":{\"empty\":true," +
                        "\"unsorted\":true,\"sorted\":false},\"offset\":0,\"paged\":true,\"unpaged\":false}," +
                        "\"last\":true,\"totalElements\":1,\"totalPages\":1,\"first\":true," +
                        "\"size\":10,\"number\":0,\"sort\":{\"empty\":true,\"unsorted\":true," +
                        "\"sorted\":false},\"numberOfElements\":1,\"empty\":false}"));
    }

    @Test
    void updateComplaint_ShouldReturnUpdatedComplaintResponse() throws Exception {

        UUID id = UUID.randomUUID();

        doReturn(id).when(complaintResponse).getId();
        doReturn("new content").when(complaintResponse).getContent();
        doReturn("Poland").when(complaintResponse).getCountry();
        doReturn(complaintResponse).when(complaintService).updateComplaint(any());

        mockMvc.perform(put("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "123",
                                    "content": "new content"
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.content").value("new content"))
                .andExpect(jsonPath("$.country").value("Poland"));
    }
}