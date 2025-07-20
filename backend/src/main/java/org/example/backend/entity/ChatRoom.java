package org.example.backend.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chatRoom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatroomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id1")
    private Member member1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id2")
    private Member member2;

    @Builder
    public ChatRoom(Member member1, Member member2) {
        this.member1 = member1;
        this.member2 = member2;
    }

    private LocalDate reg_date;
    private LocalDate update_date;
}
