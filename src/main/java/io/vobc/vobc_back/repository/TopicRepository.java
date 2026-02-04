package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.article.Topic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @EntityGraph(attributePaths = {"articles", "articles.article"})
    Optional<Topic> findWithArticlesById(Long id);
}
