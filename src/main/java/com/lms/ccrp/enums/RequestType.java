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

    public static RequestType fromLabel(String label) {
        for (RequestType requestType : values()) {
            if (requestType.getLabel().equalsIgnoreCase(label)) {
                return requestType;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + label);
    }
}
