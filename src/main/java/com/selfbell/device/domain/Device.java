package com.selfbell.device.domain;

import com.selfbell.device.domain.enums.DeviceType;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "devices")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)  // 일대일 대응 관계
    private User user;

    @Column(nullable = false)
    private String deviceToken; //fcm_token

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType = DeviceType.ANDROID; //platform
}
