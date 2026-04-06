package com.silaipro.dto.order;

import com.silaipro.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data @Builder
public class DashboardCountsResponse {
    private long todayDeliveries;
    private long overdueOrders;
    private Map<OrderStatus, Long> statusCounts;
}
