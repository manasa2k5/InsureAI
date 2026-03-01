package com.insureai.repository;

import com.insureai.model.AgentAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {


List<AgentAvailability> findByExpertiseAndLocationAndStatus(
        String expertise,
        String location,
        String status
);

}
