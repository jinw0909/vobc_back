package io.vobc.vobc_back.domain;

import io.vobc.vobc_back.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 32)
    private String author = "The VOB Foundation";

    @Column
    private LocalDate releaseDate;

    @Column(length = 512)
    private String thumbnail;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id DESC")
    private List<PostTag> postTags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Translation> translations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Post(String title, String content, String summary, String author, LocalDate releaseDate, String thumbnail) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
        this.releaseDate = releaseDate;
        this.thumbnail = thumbnail;
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

    public void setMember(Member member) {
        this.member = member;
        member.getPosts().add(this);
    }

    public static Post createPost(Member member, String title, String content, String summary, String author, LocalDate releaseDate, String thumbnail, PostTag... postTags) {

        Post post = new Post(title, content, summary, author, releaseDate, thumbnail);
        for (PostTag postTag : postTags) {
            post.addPostTag(postTag);
        }

        post.setMember(member);

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

    public void update(String title, String content, String summary, String author, LocalDate releaseDate, String thumbnail) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.releaseDate = releaseDate;
        this.thumbnail = thumbnail;
        if (author != null && !author.isBlank()) {
            this.author = author;
        }
    }

}
