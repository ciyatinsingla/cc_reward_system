package com.lms.ccrp.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    APPROVED("Approved"),
    REJECTED("Rejected"),
    POINTS_UNAVAILABLE("Points not available for use"),
    PRODUCT_AVAILABLE("Product available for redemption");

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
