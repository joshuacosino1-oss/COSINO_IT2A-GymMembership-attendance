package com.scc.model;

public record ActivityLog(int activityId, int userId, String action, String details, String timestamp) {
    @Override
    public String toString() {
        return String.format("[%d] User ID: %d | %s: %s (%s)", activityId, userId, action, details, timestamp);
    }
}