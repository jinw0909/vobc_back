package io.vobc.vobc_back.dto.roadmap;

import io.vobc.vobc_back.domain.roadmap.Roadmap;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter @Setter
@NoArgsConstructor
public class RoadmapForm {

    private Long id;
    private String title;
    private String description;
    private String content;
    private int displayOrder;
    private LocalDate roadmapDate;
    private String thumbnail;
    private boolean show;

    private List<RoadmapTranslationForm> translations = new ArrayList<>();

    public RoadmapForm(Roadmap roadmap) {
        this.id = roadmap.getId();
        this.title = roadmap.getTitle();
        this.description = roadmap.getDescription();
        this.content = roadmap.getContent();
        this.thumbnail = roadmap.getThumbnail();
        this.displayOrder = roadmap.getDisplayOrder();
        this.show = roadmap.isShow();
        this.roadmapDate = roadmap.getRoadmapDate();
    }

}
