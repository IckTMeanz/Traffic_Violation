package vn.icktmeanz.trafficViolation.controller.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateAuthorityRequest;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {
    private UserService userService;

    @Autowired
    public AdminApiController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers(){
        return this.userService.getAllUsers();
    }

    @PutMapping("/changeStatus/{id}")
    public void changeStatus(@PathVariable Long id){
        this.userService.changeStatus(id);
    }

    @PostMapping("/createAuthAcc")
    public UserDTO createAuthorityAccount(@Valid @RequestBody CreateAuthorityRequest authorityRequest){
        return this.userService.createAuthorityAccount(authorityRequest);
    }

    @PostMapping("/createUserAcc")
    public UserDTO createUserAccount(@Valid @RequestBody CreateUserRequest userRequest){
        return this.userService.createUser(userRequest);
    }
}

