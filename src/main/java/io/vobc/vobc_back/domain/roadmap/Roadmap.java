package io.vobc.vobc_back.domain.roadmap;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long id;

    @Column(length = 1024)
    private String title;

    @Column(length = 2048)
    private String description;

    @Lob
    private String content;

    private int displayOrder = 0;

    private boolean show = true;

    private LocalDate roadmapDate;

    @Column(length = 512)
    private String thumbnail;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdAt;

    @LastModifiedDate
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<RoadmapTranslation> translations = new ArrayList<>();

    public static Roadmap create(String title, String description, String content, int displayOrder, LocalDate roadmapDate, String thumbnail) {
        Roadmap roadmap = new Roadmap();
        roadmap.title = title;
        roadmap.description = description;
        roadmap.content = content;
        roadmap.displayOrder = displayOrder;
        roadmap.roadmapDate = roadmapDate;
        roadmap.thumbnail = thumbnail;
        return roadmap;
    }

    public void addTranslation(RoadmapTranslation translation) {
        translations.add(translation);
        translation.setRoadmap(this);
    }

    public void removeTranslation(RoadmapTranslation translation) {
        translations.remove(translation);
        translation.setRoadmap(null);
    }
}
