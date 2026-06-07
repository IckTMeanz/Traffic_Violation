package vn.icktmeanz.trafficViolation.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping
    public String user() {
        return "dashboard";
    }

    @GetMapping("/upload")
    public String upload() {
        return "upload-user";
    }

    @GetMapping("/history")
    public String history() {
        return "result-user";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "feedback-user";
    }
}