package com.lms.ccrp.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    APPROVED("Approved"),
    REJECTED("Rejected"),
    REQUESTED("Requested");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public static RequestStatus fromLabel(String label) {
        for (RequestStatus requestStatus : values()) {
            if (requestStatus.getLabel().equalsIgnoreCase(label)) {
                return requestStatus;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
