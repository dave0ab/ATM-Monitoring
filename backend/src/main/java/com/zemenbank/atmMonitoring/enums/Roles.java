package com.zemenbank.atmMonitoring.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Roles {

    ADMIN("ADMIN"),
    IT("IT"),
    BRANCH("BRANCH"),
    OMNI_CHANNEL("OMNI CHANNEL"),
    OMNI_CHANNEL_RECONCILIATION("OMNI OMNI_CHANNEL_RECONCILIATION");

    private final String value;

    Roles(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Roles fromValue(String value) {
        for (Roles role : Roles.values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}
