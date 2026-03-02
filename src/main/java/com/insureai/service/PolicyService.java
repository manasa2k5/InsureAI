package com.insureai.service;

import com.insureai.model.PolicyRecommendation;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    public PolicyRecommendation recommendPolicy(int age, double income, String riskLevel){

        if(age < 30 && income < 500000){
            return new PolicyRecommendation("Basic Health Plan", "Affordable coverage for young individuals");
        }

        if(age >= 30 && income >= 500000 && riskLevel.equalsIgnoreCase("Medium")){
            return new PolicyRecommendation("Comprehensive Plan", "Balanced protection with savings benefits");
        }

        if(age >= 40 || riskLevel.equalsIgnoreCase("High")){
            return new PolicyRecommendation("Premium Life Plan", "High coverage with long-term benefits");
        }

        return new PolicyRecommendation("Standard Policy", "General protection plan");
    }
}
