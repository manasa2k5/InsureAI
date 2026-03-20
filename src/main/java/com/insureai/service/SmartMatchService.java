package com.insureai.service;

import com.insureai.model.AgentAvailability;
import com.insureai.model.MatchResponse;
import com.insureai.repository.AgentAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SmartMatchService {

    @Autowired
    private AgentAvailabilityRepository repository;
public MatchResponse findBestAgent(String expertise, String location) {

    Optional<AgentAvailability> best = repository.findAll()
            .stream()
            .filter(a ->
                    a.getExpertise() != null &&
                    a.getLocation() != null &&
                    a.getStatus() != null &&
                    a.getExpertise().equalsIgnoreCase(expertise) &&
                    a.getLocation().equalsIgnoreCase(location) &&
                    a.getStatus().equalsIgnoreCase("AVAILABLE")
            )
            .findFirst();

    if (best.isPresent()) {
        return new MatchResponse(best.get().getAgentId(), 95);
    }

    return new MatchResponse(0L, 0);
}
}