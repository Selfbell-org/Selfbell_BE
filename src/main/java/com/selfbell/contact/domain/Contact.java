package com.selfbell.contact.domain;

import com.selfbell.contact.domain.enums.Status;
import com.selfbell.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "contact_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 본인
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private User contact;

    @Column(nullable = false, length = 20)
    private String relation;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "share_permission", nullable = false)
    private boolean sharePermission;

    public void accept() {
        this.status = Status.ACCEPTED;
    }

    public void updateSharePermission(boolean allow) {
        this.sharePermission = allow;
    }
}
