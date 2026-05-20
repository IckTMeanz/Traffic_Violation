package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateAuthorityRequest;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.entity.User;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    User changeStatus(Long id);

    UserDTO createUser(CreateUserRequest user);

    UserDTO createAuthorityAccount(CreateAuthorityRequest authorityRequest);
}
