package com.aube.eats.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummary(long orders, long registeredUsers, long activeMenuItems, BigDecimal revenue,
	Map<String, BigDecimal> revenueByDay, Map<String, Long> statusBreakdown) {}