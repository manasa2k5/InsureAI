package com.insureai.controller;

import com.insureai.model.Appointment;
import com.insureai.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping("/book")
    public Appointment bookAppointment(@RequestBody Appointment appointment) {
        return service.bookAppointment(appointment);
    }

    @PutMapping("/updateStatus")
public Appointment updateStatus(
        @RequestParam Long appointmentId,
        @RequestParam String status
) {
    return service.updateStatus(appointmentId, status);
}
}
