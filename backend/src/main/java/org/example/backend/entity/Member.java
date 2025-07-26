package org.example.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.backend.constant.JoinType;
import org.example.backend.constant.Role;
import org.example.backend.constant.Status;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "members")
public class Member extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long memberId;

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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Content> contents = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchingList = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsSent = new ArrayList<>();

    @OneToMany(mappedBy = "reported", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reportsReceived = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExpertProfile expertProfile;


    // 정적 팩토리 메서드
    public static Member create(String email, String encodedPassword, String nickname, String phone, JoinType joinType) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.nickname = nickname;
        member.phone = phone;
        member.role = Role.USER;         // 기본 역할 설정
        member.status = Status.ACTIVE;        // 기본 상태 설정
        member.joinType = joinType;           // 가입 유형 설정
        member.profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/default-profile.png?alt=media"; // 기본 프로필 이미지 URL 설정
        return member;
    }

    //테스트용 전문가 권한 유저생성
    public static Member createExpert(String email, String encodedPassword, String nickname, String phone, JoinType joinType) {
        Member member = new Member();
        member.email = email;
        member.password = encodedPassword;
        member.nickname = nickname;
        member.phone = phone;
        member.role = Role.EXPERT;         // 전문가 역할 설정
        member.status = Status.ACTIVE;        // 기본 상태 설정
        member.joinType = joinType;           // 가입 유형 설정
        member.profileImageUrl = "https://firebasestorage.googleapis.com/v0/b/team2maldive.firebasestorage.app/o/default-profile.png?alt=media"; // 기본 프로필 이미지 URL 설정
        return member;
    }

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }
}
