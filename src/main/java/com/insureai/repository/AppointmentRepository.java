package com.insureai.repository;

import com.insureai.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByStatus(String status);

    java.util.List<Appointment> findByCustomerId(Long customerId);

    java.util.List<Appointment> findByAgentId(Long agentId);
}