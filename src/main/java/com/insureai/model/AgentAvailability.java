package com.insureai.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_availability")
public class AgentAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long agentId;

    private String date;

    private String timeSlot;

    private String status;

    private String expertise;
    
    private String location;
    // Aprivate String expertise;

}