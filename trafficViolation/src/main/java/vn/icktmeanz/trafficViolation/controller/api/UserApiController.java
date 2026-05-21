package vn.icktmeanz.trafficViolation.controller.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.service.UserService;

@RestController
@RequestMapping("/user/api")
public class UserApiController {

    private UserService userService;

    @Autowired
    public UserApiController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/createUserAcc")
    public UserDTO createUserAccount(@Valid @RequestBody CreateUserRequest userRequest){
        return this.userService.createUser(userRequest);
    }
}
