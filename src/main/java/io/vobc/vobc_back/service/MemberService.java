package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.member.Member;
import io.vobc.vobc_back.domain.member.Role;
import io.vobc.vobc_back.dto.JoinRequest;
import io.vobc.vobc_back.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(JoinRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists: " + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.create(
                request.getEmail(),
                encodedPassword,
                Role.ROLE_ADMIN
        );

        memberRepository.save(member);
    }
}
