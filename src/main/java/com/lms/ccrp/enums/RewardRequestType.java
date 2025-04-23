package com.lms.ccrp.enums;

import lombok.Getter;

@Getter
public enum RewardRequestType {
    REDEMPTION("Redemption"),
    EARNED("Points Earned"),
    EXPIRED("Expired"),
    EXPIRED_MESSAGE("Points Expired");

    private final String typeOfRequest;

    RewardRequestType(String typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }

    public String getLabel() {
        return typeOfRequest;
    }

    public static RewardRequestType fromLabel(String label) {
        for (RewardRequestType rewardRequestType : values()) {
            if (rewardRequestType.getLabel().equalsIgnoreCase(label)) {
                return rewardRequestType;
            }
        }
        throw new IllegalArgumentException("No enum constant with label " + label);
    }

    public static String fromRewardRequestType(RewardRequestType rewardRequestType) {
        return rewardRequestType.getLabel();
    }
}
