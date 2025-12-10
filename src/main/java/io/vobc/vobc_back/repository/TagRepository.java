package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);

    @Query(value = """
        select p from Post p
        join p.postTags pt
        where pt.tag.id = :tagId
    """,
    countQuery = """
        select count(p) from Post p
        join p.postTags pt
        where pt.tag.id = :tagId
    """)
    Page<Post> findPostsByTagId(@Param("tagId") Long tagId, Pageable pageable);
}
