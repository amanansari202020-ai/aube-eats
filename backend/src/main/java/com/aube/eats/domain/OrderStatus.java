package com.aube.eats.domain;

public enum OrderStatus {
    PLACED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PLACED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == OUT_FOR_DELIVERY || next == CANCELLED;
            case OUT_FOR_DELIVERY -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}