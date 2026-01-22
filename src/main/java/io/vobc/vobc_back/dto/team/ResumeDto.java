package io.vobc.vobc_back.dto.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResumeDto {

    private Long teamMemberId;
    private Long id;
    private String content;

    public ResumeDto(Long teamMemberId, Long id, String content) {
        this.teamMemberId = teamMemberId;
        this.id = id;
        this.content = content;
    }
}
