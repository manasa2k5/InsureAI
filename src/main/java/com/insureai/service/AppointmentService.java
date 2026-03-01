package com.insureai.service;

import com.insureai.model.Appointment;
import com.insureai.model.AgentAvailability;
import com.insureai.repository.AppointmentRepository;
import com.insureai.repository.AgentAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AgentAvailabilityRepository availabilityRepository;

    public Appointment bookAppointment(Appointment appointment) {

        // Save appointment
        appointment.setStatus("BOOKED");
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Update availability status to BOOKED
        availabilityRepository.findAll().forEach(slot -> {
            if(slot.getAgentId().equals(appointment.getAgentId()) &&
               slot.getDate().equals(appointment.getDate()) &&
               slot.getTimeSlot().equals(appointment.getTimeSlot())) {

                slot.setStatus("BOOKED");
                availabilityRepository.save(slot);
            }
        });

        return savedAppointment;
    }
}
