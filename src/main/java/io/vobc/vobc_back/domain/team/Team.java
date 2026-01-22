package io.vobc.vobc_back.domain.team;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    private String name;

    private String description;

    @Column(length = 1024)
    private String icon;

    private int displayOrder = 0;

    @OrderBy("rolePriority ASC, id ASC")
    @OneToMany(mappedBy = "team", cascade ={CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    List<TeamMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamTranslation> translations = new ArrayList<>();

    //==생성 메소드==//
    public static Team create(String name, String description, String icon, int displayOrder) {
        Team team = new Team();
        team.name = name;
        team.description = description;
        team.icon = icon;
        team.displayOrder = displayOrder;
        return team;
    }

    public static Team create(Long id, String name, String description, String icon, int displayOrder) {
        Team team = create(name, description, icon, displayOrder);
        team.id = id;
        return team;
    }

    //==연관관계 편의 메서드==/
    public void addMember(TeamMember member) {
        members.add(member);
        member.setTeam(this);
    }

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void removeMember(TeamMember member) {
        members.remove(member);
        member.setTeam(null);
    }
}
