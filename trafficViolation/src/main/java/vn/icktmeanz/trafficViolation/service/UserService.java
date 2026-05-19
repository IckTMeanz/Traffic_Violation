package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.entity.User;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    User changeStatus(Long id);
}
