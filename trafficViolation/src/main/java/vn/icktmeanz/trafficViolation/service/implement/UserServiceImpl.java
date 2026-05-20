package vn.icktmeanz.trafficViolation.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.dto.request.CreateAuthorityRequest;
import vn.icktmeanz.trafficViolation.dto.request.CreateUserRequest;
import vn.icktmeanz.trafficViolation.entity.Role;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.RoleRepository;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.UserService;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = this.userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            UserDTO newUserDto = new UserDTO();
            newUserDto.setId(user.getId());
            newUserDto.setUsername(user.getUsername());
            newUserDto.setFull_name(user.getFullName());
            newUserDto.setPhone_number(user.getPhoneNumber());
            newUserDto.set_active(user.getIsActive());

            Set<Role> roles = user.getRoles();
            if (roles.stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"))) {
                newUserDto.setRole("ROLE_ADMIN");
            } else if (roles.stream().anyMatch(r -> r.getName().equals("ROLE_AUTHORITY"))) {
                newUserDto.setRole("ROLE_AUTHORITY");
            } else {
                newUserDto.setRole("ROLE_USER");
            }

            userDTOList.add(newUserDto);
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public User changeStatus(Long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        user.setIsActive(!user.getIsActive());

        return this.userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest createUserRequest) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));
        user.setPhoneNumber(createUserRequest.getPhone_number());
        user.setFullName(createUserRequest.getFull_name());
        user.setRoles(roles);
        user.setIsActive(true);
        this.userRepository.save(user);
        return UserDTO.builder().username(createUserRequest.getUsername())
                .full_name(createUserRequest.getFull_name())
                .phone_number(createUserRequest.getPhone_number())
                .role("ROLE_USER")
                .is_active(true).build();
    }

    @Override
    @Transactional
    public UserDTO createAuthorityAccount(CreateAuthorityRequest authorityRequest) {
        Role adminRole = roleRepository.findByName("ROLE_AUTHORITY")
                .orElseThrow(() -> new RuntimeException("ROLE_AUTHORITY not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        User user = new User();
        user.setUsername(authorityRequest.getUsername());
        //encode
        user.setPassword(bCryptPasswordEncoder.encode(authorityRequest.getPassword()));
        user.setPhoneNumber(authorityRequest.getPhone_number());
        user.setFullName(authorityRequest.getFull_name());
        user.setRoles(roles);
        user.setIsActive(true);
        this.userRepository.save(user);
        return UserDTO.builder().username(authorityRequest.getUsername())
                .full_name(authorityRequest.getFull_name())
                .phone_number(authorityRequest.getPhone_number())
                .role("ROLE_AUTHORITY")
                .is_active(true).build();
    }
}
