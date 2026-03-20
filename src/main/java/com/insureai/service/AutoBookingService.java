package com.insureai.service;

import com.insureai.model.AgentAvailability;
import com.insureai.model.Appointment;
import com.insureai.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoBookingService {

    @Autowired
    private AgentAvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    public Appointment autoBook(Long customerId, String expertise, String location, AppUserDetails user) {

        // Find best available agent
        AgentAvailability bestSlot = availabilityService.findBestAgent(expertise, location);

        if(bestSlot == null){
            return null;
        }

        // Create appointment automatically
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customerId);
        appointment.setAgentId(bestSlot.getAgentId());
        appointment.setDate(bestSlot.getDate());
        appointment.setTimeSlot(bestSlot.getTimeSlot());

        return appointmentService.bookAppointment(appointment, user);
    }
}