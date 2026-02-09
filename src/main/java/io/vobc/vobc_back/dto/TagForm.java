package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.Tag;
import lombok.*;

@Data
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagForm {
    private Long id;
    private String name;

    public TagForm(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
}
