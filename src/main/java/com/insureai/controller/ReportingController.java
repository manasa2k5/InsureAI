package com.insureai.controller;

import com.insureai.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportingController {

    @Autowired
    private ReportingService service;

    @GetMapping("/stats")
    public Map<String, Long> getStats(){
        return service.getAppointmentStats();
    }
}