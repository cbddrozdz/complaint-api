package pl.cbdd.complaintapi.dto;

import lombok.*;

@Getter
@Setter
public class ComplaintRequest {
    private String productId;
    private String content;
    private String reporter;
    private String country;
}