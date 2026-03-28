package com.silaipro.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_settings")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ShopSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
