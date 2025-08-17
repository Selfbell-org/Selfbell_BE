package com.selfbell.safewalk.domain;

import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "safe_walk_guardian",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_swg_session_guardian", columnNames = {"session_id", "guardian_user_id"})
    }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafeWalkGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SafeWalkSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    public static SafeWalkGuardian createGuardian(SafeWalkSession session, User guardian) {
        return SafeWalkGuardian.builder()
            .session(session)
            .guardian(guardian)
            .build();
    }
}
