package com.scc.model;

public record Membership(int membershipId, int userId, String planType, String startDate, String expiryDate, double amountPaid) {
    @Override
    public String toString() {
        return String.format("[%d] User ID: %d | Plan: %s | Expiry: %s | Amount: PHP %.2f", membershipId, userId, planType, expiryDate, amountPaid);
    }
}