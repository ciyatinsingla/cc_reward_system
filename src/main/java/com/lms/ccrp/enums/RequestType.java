package com.lms.ccrp.enums;

public enum RequestType {
    REDEMPTION("Redemption"),
    EARNED("Points Earned"),
    EXPIRED("Expired");

    private final String typeOfRequest;

    RequestType(String typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }

    public String getLabel() {
        return typeOfRequest;
    }
}
