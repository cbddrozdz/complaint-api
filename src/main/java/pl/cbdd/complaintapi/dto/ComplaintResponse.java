package pl.cbdd.complaintapi.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
public class ComplaintResponse {
    private UUID id;
    private String productId;
    private String content;
    private Timestamp createdAt;
    private String reporter;
    private String country;
    private int reportCount = 1;
}