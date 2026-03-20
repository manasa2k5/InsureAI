package com.insureai.service;

import com.insureai.model.Appointment;
import com.insureai.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

    @Autowired
    private AppointmentRepository repository;

    public Map<String, Object> getAppointmentStats(){
        try {
            Map<String, Object> stats = new HashMap<>();

            stats.put("BOOKED", repository.countByStatus("BOOKED"));
            stats.put("COMPLETED", repository.countByStatus("COMPLETED"));
            stats.put("CANCELLED", repository.countByStatus("CANCELLED"));

            // Agent utilization = number of appointments per agent.
            // This provides a simple utilization metric for the dashboard.
            List<Appointment> allAppointments = repository.findAll();
            Map<Long, Long> agentUtilization = allAppointments.stream()
                    .collect(Collectors.groupingBy(Appointment::getAgentId, Collectors.counting()));

            stats.put("agentUtilization", agentUtilization);

            return stats;
        } catch (Exception e) {
            log.error("Failed to compute reporting stats", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("BOOKED", 0);
            fallback.put("COMPLETED", 0);
            fallback.put("CANCELLED", 0);
            fallback.put("agentUtilization", Collections.emptyMap());
            return fallback;
        }
    }
}
