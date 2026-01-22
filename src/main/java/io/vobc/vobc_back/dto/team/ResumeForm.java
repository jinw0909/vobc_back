package io.vobc.vobc_back.dto.team;

import io.vobc.vobc_back.domain.team.Resume;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeForm {

    private Long teamMemberId;
    private Long id;
    private String content;

    public ResumeForm(Resume resume) {
        this.id = resume.getId();
        this.content = resume.getContent();
    }

}
