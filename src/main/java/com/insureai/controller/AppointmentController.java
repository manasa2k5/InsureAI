package com.insureai.controller;

import com.insureai.model.Appointment;
import com.insureai.security.AppUserDetails;
import com.insureai.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    // 📌 Book Appointment (CUSTOMER / ADMIN)
    @PostMapping("/book")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public Appointment bookAppointment(@AuthenticationPrincipal AppUserDetails user,
                                       @RequestBody Appointment appointment) {
        return service.bookAppointment(appointment, user);
    }

    // 📌 Update Appointment Status (CUSTOMER / ADMIN)
    @PutMapping("/updateStatus")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public Appointment updateStatus(@AuthenticationPrincipal AppUserDetails user,
                                    @RequestParam Long appointmentId,
                                    @RequestParam String status) {
        return service.updateStatus(appointmentId, status, user);
    }

    // 📌 Get All Appointments
    @GetMapping
    public List<Appointment> getAllAppointments(@AuthenticationPrincipal AppUserDetails user) {
        return service.getAllAppointments(user);
    }
}
