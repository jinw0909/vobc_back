package io.vobc.vobc_back.repository.publisher;

import io.vobc.vobc_back.domain.publisher.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    interface PublisherIdName {
        Long getId();
        String getName();
    }

    List<PublisherIdName> findAllByOrderByName();
}
