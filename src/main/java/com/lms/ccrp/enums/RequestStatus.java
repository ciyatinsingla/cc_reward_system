package com.lms.ccrp.enums;

public enum RequestStatus {
    APPROVED("Approved"),
    REJECTED("Rejected"),
    POINTS_UNAVAILABLE("Points not available for use"),
    PRODUCT_AVAILABLE("Product available for redemption");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
