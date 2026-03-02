package com.insureai.service;

import com.insureai.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportingService {

    @Autowired
    private AppointmentRepository repository;

    public Map<String, Long> getAppointmentStats(){

        Map<String, Long> stats = new HashMap<>();

        stats.put("BOOKED", repository.countByStatus("BOOKED"));
        stats.put("COMPLETED", repository.countByStatus("COMPLETED"));
        stats.put("CANCELLED", repository.countByStatus("CANCELLED"));

        return stats;
    }
}
