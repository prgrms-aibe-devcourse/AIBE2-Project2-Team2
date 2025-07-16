package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "expert_profile_specialty_detail")
public class ExpertProfileSpecialtyDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_profile_id", nullable = false)
    private ExpertProfile expertProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_field_id", nullable = false)
    private DetailField detailField;

    public ExpertProfileSpecialtyDetail(ExpertProfile expertProfile, Specialty specialty, DetailField detailField) {
        this.expertProfile = expertProfile;
        this.specialty = specialty;
        this.detailField = detailField;
    }
}
