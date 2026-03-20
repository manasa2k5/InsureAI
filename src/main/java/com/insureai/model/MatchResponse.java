package com.insureai.model;

public class MatchResponse {

    private Long agentId;
    private int score;

    public MatchResponse(Long agentId, int score) {
        this.agentId = agentId;
        this.score = score;
    }

    public Long getAgentId() {
        return agentId;
    }

    public int getScore() {
        return score;
    }
}