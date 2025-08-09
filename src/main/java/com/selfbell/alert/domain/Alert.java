package com.selfbell.alert.domain;

import com.selfbell.address.domain.Address;
import com.selfbell.criminal.domain.Criminal;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @Builder.Default
    @Min(100)
    @Max(1000)
    @Column(nullable = false, columnDefinition = "int default 500")
    private int radiusM = 500;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    private Boolean isViewed = false;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column(nullable = false)
    private LocalDateTime sendAt;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criminal_id", nullable = false)
    private Criminal criminal;
}
