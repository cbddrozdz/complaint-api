package pl.cbdd.complaintapi.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateComplaintRequest {
    private String content;
}