package com.selfbell.sos.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sos_template")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SosTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String title;

    private String content;
}
