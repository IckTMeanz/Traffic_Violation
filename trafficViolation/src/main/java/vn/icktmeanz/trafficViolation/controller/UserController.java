package vn.icktmeanz.trafficViolation.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class UserController {
    
    @GetMapping("/admin")
    public String admin() {
        return "admin2";
    }

    @GetMapping("/user")
    public String user() {
        return "dashboard";
    }
}