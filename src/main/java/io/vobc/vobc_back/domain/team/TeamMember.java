package io.vobc.vobc_back.domain.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @JsonIgnore
    @OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMemberTranslation> translations = new ArrayList<>();

    private String name;

    private String email;

    @Column(name = "INTRODUCTION", length = 2048)
    private String introduction;

    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private TeamRole role;

    @Column(length = 1024)
    private String photo;

    @Column(name = "ROLE_PRIORITY", nullable = true)
    private Integer rolePriority = 0;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static TeamMember create(Team team, String name, TeamRole role, String photo) {
        TeamMember teamMember = new TeamMember();
        teamMember.team = team;
        teamMember.name = name;
        teamMember.role = role;
        teamMember.photo = photo;
        teamMember.rolePriority = role.getPriority();
        team.addMember(teamMember);
        return teamMember;
    }

    public void removeResume(Resume resume) {
        resumes.remove(resume);
        resume.setTeamMember(null);
    }

    public void changeTeam(Team team) {
        if (this.team != null) {
            this.team.getMembers().remove(this); //기존 컬렉션에서 제거
        }
        this.team = team;
        if (team != null && !team.getMembers().contains(this)) {
            team.getMembers().add(this);
        }
    }

    public void addResume(Resume resume) {
        this.resumes.add(resume);
        resume.setTeamMember(this);
    }

    public void reOrder() {
        this.rolePriority = (role != null ? role.getPriority() : 10);
    }

}
