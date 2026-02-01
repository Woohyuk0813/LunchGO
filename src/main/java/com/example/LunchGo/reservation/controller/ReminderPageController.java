package com.example.LunchGo.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReminderPageController {

    @GetMapping("/reminders/visit")
    public String visitPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("mode", "visit");
        return "reminder";
    }

    @GetMapping("/reminders/cancel")
    public String cancelPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("mode", "cancel");
        return "reminder";
    }

    @GetMapping("/reminders/result")
    public String resultPage(@RequestParam(name = "result", required = false) String result, Model model) {
        model.addAttribute("result", result == null ? "invalid" : result);
        return "reminder-result";
    }
}