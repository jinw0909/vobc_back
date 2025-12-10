package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.dto.TagForm;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class PostForm {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String author;
    private LocalDate releaseDate;
    private String thumbnail;

//    private List<TagForm> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostForm from(Post p) {
        PostForm f = new PostForm();
        f.setId(p.getId());
        f.setTitle(p.getTitle());
        f.setContent(p.getContent());
        f.setSummary(p.getSummary());
        f.setAuthor(p.getAuthor());
        f.setReleaseDate(p.getReleaseDate());
        f.setThumbnail(p.getThumbnail());

//        List<TagForm> tagFormList = p.getPostTags().stream()
//                .map(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()))
//                .toList();
//
//        f.setTags(tagFormList);

        return f;
    }
}
