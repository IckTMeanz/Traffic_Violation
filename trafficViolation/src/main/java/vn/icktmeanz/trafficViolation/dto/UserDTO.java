package vn.icktmeanz.trafficViolation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String full_name;
    private String phone_number;
    private String role;
    private boolean is_active;
}
