package com.insureai.controller;

import com.insureai.model.Appointment;
import com.insureai.security.AppUserDetails;
import com.insureai.service.AutoBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto")
@CrossOrigin
public class AutoBookingController {

    @Autowired
    private AutoBookingService service;

    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Appointment autoBook(
            @RequestParam Long customerId,
            @RequestParam String expertise,
            @RequestParam String location,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return service.autoBook(customerId, expertise, location, user);
    }
}