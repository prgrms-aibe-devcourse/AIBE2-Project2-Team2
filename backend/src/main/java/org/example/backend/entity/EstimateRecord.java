package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estimate_record")
@Getter
@NoArgsConstructor
public class EstimateRecord extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estimate_record_id")
    private Long EstimateRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @OneToMany(mappedBy = "estimateRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedProduct> selectedProducts = new ArrayList<>();
}
