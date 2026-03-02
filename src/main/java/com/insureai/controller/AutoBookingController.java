package com.insureai.controller;

import com.insureai.model.Appointment;
import com.insureai.service.AutoBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto")
@CrossOrigin
public class AutoBookingController {

    @Autowired
    private AutoBookingService service;

    @PostMapping("/book")
    public Appointment autoBook(
            @RequestParam Long customerId,
            @RequestParam String expertise,
            @RequestParam String location
    ) {
        return service.autoBook(customerId, expertise, location);
    }
}