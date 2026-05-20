package vn.icktmeanz.trafficViolation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String full_name;
    private String phone_number;
    private String role;
    @JsonProperty("is_active")
    private boolean is_active;
}
