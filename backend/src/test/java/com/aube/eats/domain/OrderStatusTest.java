package com.aube.eats.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {
    @Test
    void statusFlowOnlyMovesForward() {
        assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.PREPARING));
        assertTrue(OrderStatus.PREPARING.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY));
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PREPARING));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DELIVERED));
    }
}