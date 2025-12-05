package io.vobc.vobc_back.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Translation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_translation_id")
    private Long id;

    @Column(nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    private LanguageCode languageCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String summary;

    @Column(length = 32)
    private String author = "The VOB Foundation";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public Translation(LanguageCode languageCode, String title, String content, String summary, String author) {
        this.languageCode = languageCode;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
    }

    void setPost(Post post) {
        this.post = post;
    }


}

