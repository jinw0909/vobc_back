package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String username);

    boolean existsByEmail(String email);
}
