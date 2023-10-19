package com.example.gatewayservice.dto;

public class CustomDto {
    private Boolean logging;

    public CustomDto(Boolean logging) {
        this.logging = logging;
    }

    public CustomDto() {
    }

    public Boolean getLogging() {
        return logging;
    }

    public void setLogging(Boolean logging) {
        this.logging = logging;
    }
}
