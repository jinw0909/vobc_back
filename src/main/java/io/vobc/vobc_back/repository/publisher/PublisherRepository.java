package io.vobc.vobc_back.repository.publisher;

import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.dto.publisher.PublisherForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

//    @Query("select p from Publisher p where lower(p.name) like lower(concat('%', :name, '%'))")
//    Page<Publisher> search(@Param("name") String name, Pageable pageable);

    @Query("""
        select p
        from Publisher p
        where lower(p.name) like lower(concat('%', :keyword, '%'))
           or exists (
             select 1
             from PublisherTranslation t
             where t.publisher = p
               and lower(t.name) like lower(concat('%', :keyword, '%'))
           )
    """)
    Page<Publisher> search(String keyword, Pageable pageable);

    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        select distinct p.id
        from Publisher p
        left join p.translations t
        where lower(p.name) like lower(concat('%', :keyword, '%'))
           or lower(t.name) like lower(concat('%', :keyword, '%'))
    """)
    Page<Long> searchIds(@Param("keyword") String keyword, Pageable pageable);

    @Query("select p from Publisher p where p.id in :ids")
    List<Publisher> findAllByIdIn(@Param("ids") List<Long> ids);

    interface PublisherIdName {
        Long getId();
        String getName();
    }

    List<PublisherIdName> findAllByOrderByName();
}
