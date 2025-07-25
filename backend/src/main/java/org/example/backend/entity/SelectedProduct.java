package org.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "selected_product")
@Getter
@NoArgsConstructor
@AllArgsConstructor // 추가
@Builder // 추가
public class SelectedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selected_product_id")
    private Long SelectedProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimate_record_id", nullable = false)
    private EstimateRecord estimateRecord;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;
}
