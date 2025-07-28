package org.example.backend.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {
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

    /** ✅ 1:N 관계 매핑 (ChatRoom → ChatMessage) */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sendAt ASC") // 메시지를 보낸 순서대로 정렬
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "last_message_time")
    @Setter(AccessLevel.PUBLIC)
    private LocalDateTime lastMessageTime;
}
