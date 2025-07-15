package org.example.backend.entity;


import lombok.Getter;
import org.example.backend.constant.JoinType;
import org.example.backend.constant.Role;
import org.example.backend.constant.Status;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name= "skills")
    private String skills;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "join_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private JoinType joinType;

    // 정적 팩토리 메서드
    public static User create(String email, String encodedPassword, String nickname, String phone, JoinType joinType) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.nickname = nickname;
        user.phone = phone;
        user.role = Role.USER;         // 기본 역할 설정
        user.status = Status.ACTIVE;        // 기본 상태 설정
        user.joinType = joinType;           // 가입 유형 설정
        return user;
    }

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }
}
