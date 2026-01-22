package io.vobc.vobc_back.domain.team;

import io.vobc.vobc_back.domain.LanguageCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ResumeTranslation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LanguageCode languageCode;

    private String content;

    public static ResumeTranslation create(Resume resume, LanguageCode languageCode, String content) {
        ResumeTranslation resumeTranslation = new ResumeTranslation();
        resumeTranslation.resume = resume;
        resumeTranslation.languageCode = languageCode;
        resumeTranslation.content = content;
        resume.getTranslations().add(resumeTranslation);
        return resumeTranslation;
    }
}
