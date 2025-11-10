package com.balaio.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/balaio/login";
    }

    @GetMapping("/balaio")
    public String webHome() {
        return "redirect:/balaio/login";
    }
}