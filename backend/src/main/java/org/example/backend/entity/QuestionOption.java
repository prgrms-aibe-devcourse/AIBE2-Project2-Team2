package org.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "question_option")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "additional_price", nullable = false)
    private Long additionalPrice = 0L; // 추가금 기본 설정 0원으로 -> 옵션별 0원인 경우에도 사용할 수 있도록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    public QuestionOption(String optionText, Long additionalPrice) {
        this.optionText = optionText;
        this.additionalPrice = additionalPrice;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
