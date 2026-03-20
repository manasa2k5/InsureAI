package com.insureai.service;

import com.insureai.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String from,
                        @Value("${insureai.notification.email.to}") String to) {
        this.mailSender = mailSender;
        this.from = from;
        this.to = to;
    }

    public void sendBookingNotification(Appointment appointment) {
        sendAppointmentNotification(appointment, "BOOKED");
    }

    public void sendCancellationNotification(Appointment appointment) {
        sendAppointmentNotification(appointment, "CANCELLED");
    }

    private void sendAppointmentNotification(Appointment appointment, String status) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(String.format("[InsureAI] Appointment %s - Agent %s", status, appointment.getAgentId()));
            message.setText(buildEmailBody(appointment, status));

            mailSender.send(message);
            log.info("Sent appointment email ({}): appointmentId={}", status, appointment.getId());
        } catch (MailException e) {
            log.warn("Unable to send appointment notification email: {}", e.getMessage());
        }
    }

    private String buildEmailBody(Appointment appointment, String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("InsureAI Appointment Notification\n\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Appointment ID: ").append(appointment.getId()).append("\n");
        sb.append("Agent ID: ").append(appointment.getAgentId()).append("\n");
        sb.append("Date: ").append(appointment.getDate()).append("\n");
        sb.append("Time Slot: ").append(appointment.getTimeSlot()).append("\n\n");
        sb.append("If you did not expect this email, please contact support.");
        return sb.toString();
    }
}
