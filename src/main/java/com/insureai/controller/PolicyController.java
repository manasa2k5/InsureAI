package com.insureai.controller;

import com.insureai.model.PolicyRecommendation;
import com.insureai.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
@CrossOrigin
public class PolicyController {

    @Autowired
    private PolicyService service;

    @GetMapping("/recommend")
    public PolicyRecommendation recommend(
            @RequestParam int age,
            @RequestParam double income,
            @RequestParam String riskLevel
    ){
        return service.recommendPolicy(age, income, riskLevel);
    }
}
