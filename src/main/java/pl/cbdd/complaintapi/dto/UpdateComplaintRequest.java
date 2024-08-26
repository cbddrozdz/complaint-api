package pl.cbdd.complaintapi.dto;

import lombok.*;

@Getter
@Setter
public class UpdateComplaintRequest {
    private String id;
    private String content;
}