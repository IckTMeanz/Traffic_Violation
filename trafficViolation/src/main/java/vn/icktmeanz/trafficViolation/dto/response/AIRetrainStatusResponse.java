package vn.icktmeanz.trafficViolation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AIRetrainStatusResponse {
    @JsonProperty("is_running")
    private Boolean isRunning;

    private String message;

    private Integer epoch;

    @JsonProperty("best_f1")
    private Double bestF1;
}
