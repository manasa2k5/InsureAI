package com.insureai.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRecommendation {

    private String policyType;
    private String recommendation;
}
