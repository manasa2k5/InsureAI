package com.insureai.service;

import com.insureai.model.AgentAvailability;
import com.insureai.repository.AgentAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentAvailabilityService {

    @Autowired
    private AgentAvailabilityRepository repository;

    public AgentAvailability addAvailability(AgentAvailability availability) {
        availability.setStatus("AVAILABLE");
        return repository.save(availability);
    }

    public List<AgentAvailability> getAllAvailability() {
        return repository.findAll();
    }
}
