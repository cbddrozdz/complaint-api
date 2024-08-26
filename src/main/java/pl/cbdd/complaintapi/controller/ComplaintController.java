package pl.cbdd.complaintapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cbdd.complaintapi.dto.ComplaintRequest;
import pl.cbdd.complaintapi.dto.ComplaintResponse;

import pl.cbdd.complaintapi.dto.UpdateComplaintRequest;
import pl.cbdd.complaintapi.errorhandling.ErrorResponse;
import pl.cbdd.complaintapi.service.ComplaintService;
import pl.cbdd.complaintapi.service.GeoLocationService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final GeoLocationService geoLocationService;

    @Operation(summary = "Add a new complaint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Complaint added successfully", content = @Content(schema = @Schema(implementation = ComplaintResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - Error in external GeoLocation service", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ComplaintResponse> addComplaint(@RequestBody ComplaintRequest complaintRequest, HttpServletRequest request) {

        String clientIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElse(request.getRemoteAddr());
        complaintRequest.setCountry(geoLocationService.getCountryByIp(clientIp));

        // complaintRequest.setCountry(geoLocationService.getCountryByIp(request.getRemoteAddr()));

        return ResponseEntity.ok().body(complaintService.addComplaint(complaintRequest));
    }

    @Operation(summary = "Get a complaint by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Complaint retrieved successfully", content = @Content(schema = @Schema(implementation = ComplaintResponse.class))),
            @ApiResponse(responseCode = "404", description = "Complaint not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaint(@PathVariable UUID id) {
        return ResponseEntity.ok().body(complaintService.getComplaint(id));
    }

    @Operation(summary = "Get all complaints")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Complaints retrieved successfully", content = @Content(schema = @Schema(implementation = ComplaintResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<Page<ComplaintResponse>> getAll(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok().body(complaintService.getAllComplaints(pageable));
    }

    @Operation(summary = "Update a complaint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Complaint updated successfully", content = @Content(schema = @Schema(implementation = ComplaintResponse.class))),
            @ApiResponse(responseCode = "404", description = "Complaint not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<ComplaintResponse> updateComplaint(@Valid @RequestBody UpdateComplaintRequest updateComplaintRequest) {
        return ResponseEntity.ok().body(complaintService.updateComplaint(updateComplaintRequest));
    }
}