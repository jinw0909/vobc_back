package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.term.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermQueryRepository extends JpaRepository<Term, Long> {
}
