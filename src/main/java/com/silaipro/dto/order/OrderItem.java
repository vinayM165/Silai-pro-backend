package com.silaipro.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItem {
    private String description;
    private String fabricDetails;
    private Long measurementId;
    private Integer quantity;
    private BigDecimal pricePerPiece;
    private BigDecimal amount;
}
