package org.example.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.backend.constant.PaymentStatus;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "Payment")
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(nullable = false)
    private Long cost;

    @Column(nullable = false)
    private String tid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    // regTime(생성일시) getter 별도 노출
    public java.time.LocalDateTime getCreatedDate() {
        return getRegTime();
    }
}