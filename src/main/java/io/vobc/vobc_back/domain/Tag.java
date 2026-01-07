package io.vobc.vobc_back.domain;

import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.PostTag;
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
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PostTag> postTags = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Tag(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public List<Post> getPosts() {
        return postTags.stream().map(PostTag::getPost).toList();
    }

    public void addPostTag(PostTag postTag) {
        postTags.add(postTag);
        postTag.setTag(this);
    }
}
