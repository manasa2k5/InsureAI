package com.insureai.service;

import com.insureai.model.Appointment;
import com.insureai.model.AgentAvailability;
import com.insureai.repository.AppointmentRepository;
import com.insureai.repository.AgentAvailabilityRepository;
import com.insureai.security.AppUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AgentAvailabilityRepository availabilityRepository;

    @Autowired
    private EmailService emailService;

    // 🔹 Book Appointment (enforces customer identity for CUSTOMER role)
    public Appointment bookAppointment(Appointment appointment, AppUserDetails user) {
        if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
            appointment.setCustomerId(user.getId());
        }

        appointment.setStatus("BOOKED");
        Appointment savedAppointment = appointmentRepository.save(appointment);

        availabilityRepository.findAll().forEach(slot -> {
            if (slot.getAgentId().equals(appointment.getAgentId()) &&
                slot.getDate().equals(appointment.getDate()) &&
                slot.getTimeSlot().equals(appointment.getTimeSlot())) {

                slot.setStatus("BOOKED");
                availabilityRepository.save(slot);
            }
        });

        try {
            emailService.sendBookingNotification(savedAppointment);
        } catch (Exception e) {
            log.warn("Failed to send booking notification email: {}", e.getMessage());
        }

        return savedAppointment;
    }

    // 🔹 Update Status (CUSTOMER can cancel their own appointment)
    public Appointment updateStatus(Long appointmentId, String status, AppUserDetails user){

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);

        if (appointment == null) {
            return null;
        }

        // Customers may only update their own appointments
        boolean isCustomer = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        if (isCustomer && !appointment.getCustomerId().equals(user.getId())) {
            return null;
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        if (status.equals("CANCELLED")) {
            availabilityRepository.findAll().forEach(slot -> {
                if (slot.getAgentId().equals(appointment.getAgentId()) &&
                   slot.getDate().equals(appointment.getDate()) &&
                   slot.getTimeSlot().equals(appointment.getTimeSlot())) {

                    slot.setStatus("AVAILABLE");
                    availabilityRepository.save(slot);
                }
            });

            try {
                emailService.sendCancellationNotification(updated);
            } catch (Exception e) {
                log.warn("Failed to send cancellation notification email: {}", e.getMessage());
            }
        }

        return updated;
    }

    // 🔹 History Support
    public List<Appointment> getAllAppointments(AppUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isAgent = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));

        if (isAdmin) {
            return appointmentRepository.findAll();
        }

        if (isAgent) {
            return appointmentRepository.findByAgentId(user.getId());
        }

        // CUSTOMER
        return appointmentRepository.findByCustomerId(user.getId());
    }
}