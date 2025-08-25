package com.selfbell.sos.domain;

import com.selfbell.global.entity.BaseTimeEntity;
import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "sos_message")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SosMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    @Column(name = "template_id", nullable = true)
    private Long templateId;

    private String message;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "lat", precision = 10, scale = 7)),
            @AttributeOverride(name = "lon", column = @Column(name = "lon", precision = 10, scale = 7))
    })
    private GeoPoint point;

    public static SosMessage createSosMessage(Long userId, Long templateId, String message, GeoPoint point) {
        return SosMessage.builder()
                .sender(User.builder().id(userId).build())
                .templateId(templateId)
                .message(message)
                .point(point)
                .build();
    }
}
