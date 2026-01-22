package io.vobc.vobc_back.domain.team;

import io.vobc.vobc_back.domain.LanguageCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMemberTranslation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LanguageCode languageCode;

    private String name;

    @Column(name = "INTRODUCTION", length = 2048)
    private String introduction;

    public static TeamMemberTranslation create(TeamMember teamMember, LanguageCode languageCode, String name, String introduction) {
        TeamMemberTranslation translation = new TeamMemberTranslation();
        translation.teamMember = teamMember;
        translation.languageCode = languageCode;
        translation.name = name;
        translation.introduction = introduction;
        teamMember.getTranslations().add(translation);
        return translation;
    }


}
