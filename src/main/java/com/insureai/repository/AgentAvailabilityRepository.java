package com.insureai.repository;

import com.insureai.model.AgentAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentAvailabilityRepository extends JpaRepository<AgentAvailability, Long> {
}
