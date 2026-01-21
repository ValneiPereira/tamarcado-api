package com.tamarcado.domain.model.user;

import com.tamarcado.domain.model.service.Category;
import com.tamarcado.domain.model.service.ServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "professionals", indexes = {
    @Index(name = "idx_professional_category", columnList = "category"),
    @Index(name = "idx_professional_service_type", columnList = "serviceType"),
    @Index(name = "idx_professional_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Professional {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}