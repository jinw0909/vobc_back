package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.PostTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @EntityGraph(attributePaths = "tag")
    List<PostTag> findByPost_IdIn(List<Long> postId, Sort sort);
}
