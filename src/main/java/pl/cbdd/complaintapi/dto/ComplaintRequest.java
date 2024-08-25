package pl.cbdd.complaintapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintRequest {

    private String productId;
    private String content;
    private String reporter;
    private String country;
}