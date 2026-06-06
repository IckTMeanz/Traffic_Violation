package vn.icktmeanz.trafficViolation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeViolationTypeRequest {
    int no_helmet;
    int using_phone;
    int triple_riding;
}
