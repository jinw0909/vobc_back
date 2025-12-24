package io.vobc.vobc_back.domain.publisher;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.dto.publisher.PublisherForm;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublisherTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_translation_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LanguageCode languageCode;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static PublisherTranslation create(LanguageCode languageCode, String name, String introduction, Publisher publisher) {
        PublisherTranslation translation = new PublisherTranslation();
        translation.languageCode = languageCode;
        translation.name = name;
        translation.introduction = introduction;
        publisher.addTranslation(translation);
        return translation;
    }

    @Override
    public String toString() {
        return "PublisherTranslation{" +
                "id=" + id +
                ", languageCode=" + languageCode +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                ", publisher=" + publisher +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
