package io.vobc.vobc_back.domain.term;

import io.vobc.vobc_back.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private TermCode termCode;

    @Setter
    private String title;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String content;

    @Setter
    private LocalDate proposeDate;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<TermTranslation> translations = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    //==생성 메서드==//
    public static Term create(TermCode termCode, String title, String content, LocalDate proposeDate) {
        if (termCode == null) throw new IllegalArgumentException("termCode must not be null");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be null or blank");

        Term term = new Term();
        term.termCode = termCode;
        term.title = title;
        term.content = content;
        term.proposeDate = proposeDate;
        return term;
    }

    //==연관관계 편의 메서드==//
    public void addTranslation(TermTranslation translation) {
        translations.add(translation);
        translation.setTerm(this);
    }

    public void removeTranslation(TermTranslation translation) {
        translations.remove(translation);
    }

}

