package com.insureai.controller;

import com.insureai.model.AgentAvailability;
import com.insureai.service.AgentAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@CrossOrigin
public class AgentAvailabilityController {

    @Autowired
    private AgentAvailabilityService service;

    @PostMapping("/add")
    public AgentAvailability addAvailability(@RequestBody AgentAvailability availability) {
        return service.addAvailability(availability);
    }

    @GetMapping("/all")
    public List<AgentAvailability> getAllAvailability() {
        return service.getAllAvailability();
    }

    @GetMapping("/match")
public AgentAvailability matchAgent(
        @RequestParam String expertise,
        @RequestParam String location
) {
    return service.findBestAgent(expertise, location);
}

}