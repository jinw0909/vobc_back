package io.vobc.vobc_back.repository.term;

import io.vobc.vobc_back.domain.term.Term;
import io.vobc.vobc_back.domain.term.TermCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    @EntityGraph(attributePaths = {"translations"})
    Optional<Term> findWithTranslationsById(Long id);

    @EntityGraph(attributePaths = {"translations"})
    Optional<Term> findByTermCode(TermCode termCode);

    List<Term> findAllByOrderByIdAsc();
}
