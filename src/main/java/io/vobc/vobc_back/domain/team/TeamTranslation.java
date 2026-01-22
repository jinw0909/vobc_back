package io.vobc.vobc_back.domain.team;

import io.vobc.vobc_back.domain.LanguageCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TeamTranslation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LanguageCode languageCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    public static TeamTranslation create(Team team, LanguageCode languageCode, String name, String description) {
        TeamTranslation teamTranslation = new TeamTranslation();
        teamTranslation.team = team;
        teamTranslation.languageCode = languageCode;
        teamTranslation.name = name;
        teamTranslation.description = description;
        team.getTranslations().add(teamTranslation);
        return teamTranslation;
    }
}
