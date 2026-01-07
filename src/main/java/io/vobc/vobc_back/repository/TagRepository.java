package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);

//    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
//    Page<Post> findByPostTags_Tag_Id(Long tagId, Pageable pageable);
}
