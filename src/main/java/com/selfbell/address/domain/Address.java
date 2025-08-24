package com.selfbell.address.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class Address extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private BigDecimal lat;

    @Column(nullable = false)
    private BigDecimal lon;

    public static Address create(User user, String name, String address, BigDecimal lat, BigDecimal lon) {
        return Address.builder()
                .user(user)
                .name(name)
                .address(address)
                .lat(lat)
                .lon(lon)
                .build();
    }
}
