package io.vobc.vobc_back.dto;

import io.vobc.vobc_back.domain.article.Topic;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopicForm {

    private Long id;
    @NotBlank
    private String name;

    public TopicForm(Topic topic) {
        this.id = topic.getId();
        this.name = topic.getName();
    }
}
