package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;     // 추가
import lombok.Builder;               // 추가

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estimate_record")
@Getter
@NoArgsConstructor
@AllArgsConstructor // 추가
@Builder            // 추가
public class EstimateRecord extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estimate_record_id")
    private Long EstimateRecordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Builder.Default // 추가 (빌더 사용시에도 빈 리스트로 초기화)
    @OneToMany(mappedBy = "estimateRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedProduct> selectedProducts = new ArrayList<>();

}
