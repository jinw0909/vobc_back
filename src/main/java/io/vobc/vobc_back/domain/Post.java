package io.vobc.vobc_back.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String summary;

    @Column(length = 32)
    private String author = "The VOB Foundation";

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Translation> translations = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Post(String title, String content, String summary, String author) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
    }

    // 연관관계 메서드

    public void addPostTag(PostTag postTag) {
        postTags.add(postTag);
        postTag.setPost(this);
    }


    public void addTranslation(Translation translation) {
        translations.add(translation);
        translation.setPost(this);
    }

    public static Post createPost(String title, String content, String summary, String author, PostTag... postTags) {
        Post post = new Post(title, content, summary, author);
        for (PostTag postTag : postTags) {
            post.addPostTag(postTag);
        }
        return post;
    }

    // 편의 메서드
    public PostTag addTag(Tag tag) {
        PostTag postTag = new PostTag(this, tag);
        postTags.add(postTag); //Post 쪽 컬렉션
        tag.getPostTags().add(postTag); //Tag 쪽 컬렉션
        return postTag;
    }

    public void removeTag(Tag tag) {
        postTags.removeIf(pt -> {
            if (pt.getTag().equals(tag)) {
                tag.getPostTags().remove(pt);
                pt.setPost(null);
                pt.setTag(null);
                return true;
            }
            return false;
        });
    }

    public void update(String title, String content, String summary, String author) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        if (author != null && !author.isBlank()) {
            this.author = author;
        }
    }
}
