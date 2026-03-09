package com.example.LifeMaster_BE.Group;

public enum GroupAccessType {
    PUBLIC(false),
    PRIVATE(true),
    PASSWORD(true);

    private final boolean requiresPassword;

    GroupAccessType(boolean requiresPassword) {
        this.requiresPassword = requiresPassword;
    }

    public boolean requiresPassword() {
        return requiresPassword;
    }
}