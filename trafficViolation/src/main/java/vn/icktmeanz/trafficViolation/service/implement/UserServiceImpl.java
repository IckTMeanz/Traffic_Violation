package vn.icktmeanz.trafficViolation.service.implement;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.icktmeanz.trafficViolation.dto.UserDTO;
import vn.icktmeanz.trafficViolation.entity.Role;
import vn.icktmeanz.trafficViolation.entity.User;
import vn.icktmeanz.trafficViolation.repository.UserRepository;
import vn.icktmeanz.trafficViolation.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = this.userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for(User user : users){
            UserDTO newUserDto = new UserDTO();
            newUserDto.setId(user.getId());
            newUserDto.setUsername(user.getUsername());
            newUserDto.setFull_name(user.getFullName());
            newUserDto.setPhone_number(user.getPhoneNumber());
            newUserDto.set_active(user.getIsActive());
            Set<Role> roles = user.getRoles();
            if (roles.stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                newUserDto.setRole("ROLE_ADMIN");

            } else if (roles.stream().anyMatch(role -> role.getName().equals("ROLE_AUTHORITY"))) {
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
        User user = this.userRepository.findById(id).orElseThrow(()-> new RuntimeException());
        if(user.getIsActive()==true){
            user.setIsActive(false);
        }else{
            user.setIsActive(true);
        }
        return user;
    }
}
