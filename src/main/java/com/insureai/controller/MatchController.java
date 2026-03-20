package com.insureai.controller;

import com.insureai.model.MatchResponse;
import com.insureai.service.SmartMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MatchController {

    @Autowired
    private SmartMatchService smartMatchService;

    @GetMapping("/match")
    public MatchResponse matchAgent(
            @RequestParam String expertise,
            @RequestParam String location
    ) {
        return smartMatchService.findBestAgent(expertise, location);
    }
}
