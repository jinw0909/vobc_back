package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Tag;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select distinct p
        from Post p
        join fetch p.postTags pt
        where pt.tag in :tags
            and p.id <> :postId
    """)
    List<Post> findCandidatesByTags(@Param("tags") List<Tag> tags,
                                    @Param("postId") Long postId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    Page<Post> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    Optional<Post> findWithTagsById(Long id);

    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    Page<Post> findByPostTags_Tag_Id(Long tagId, Pageable pageable);
}
