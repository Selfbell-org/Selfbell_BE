package com.selfbell.sos.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import com.selfbell.sos.domain.enums.SosStatus;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sos_recipient")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SosRecipient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sos_id", nullable = false)
    private SosMessage sos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosStatus status;

    public static SosRecipient createSosRecipient(SosMessage sos, User recipient) {
        return SosRecipient.builder()
                .sos(sos)
                .recipient(recipient)
                .status(SosStatus.IN_PROGRESS)
                .build();
    }
}
