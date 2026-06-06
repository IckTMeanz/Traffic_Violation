package vn.icktmeanz.trafficViolation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/authority")
public class AuthorityController {
    @GetMapping("/upload") 
    public String author() {
        return "upload-aut";
    }

    @GetMapping("/result")
    public String result() {
        return "result-aut";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "feedback-aut";
    }

    @GetMapping("/statistic")
    public String statistic() {
        return "statistc-aut";
    }
}
